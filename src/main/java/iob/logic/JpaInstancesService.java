package iob.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iob.data.InstanceEntity;
import iob.data.InstancesDao;
import iob.data.UserDao;
import iob.data.UserEntity;
import iob.data.UserRole;
import iob.restapi.boundaries.InstanceBoundary;
import iob.restapi.objects.CreatedBy;
import iob.restapi.objects.InstanceId;
import iob.restapi.objects.Location;
import iob.restapi.objects.UserId;

@Service
public class JpaInstancesService implements EnhancedInstancesService {

	private InstancesDao instancesDao;
	private ObjectMapper jackson;
	private UserDao userDao;

	@Autowired
	public JpaInstancesService(InstancesDao instancesDao, UserDao userDao) {
		this.instancesDao = instancesDao;
		this.userDao = userDao;

	}

	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}

	@Override
	@Transactional
	public InstanceBoundary createInstance(InstanceBoundary instance) {
		InstanceEntity instanceEntity = this.InstanceBoundaryToEntity(instance);
		if (instanceEntity != null) {
			instanceEntity = this.instancesDao.save(instanceEntity);
			return this.InstanceEntityToBoundary(instanceEntity);
		} else
			throw new RuntimeException("Failed to create an instance!");
	}

	@Override
	@Deprecated
	public InstanceBoundary updateInstnce(String instanceDomain, String instanceId, InstanceBoundary update) {
		throw new RuntimeException("deprecated , updateInstnceAccordingRole instead");

	}

	@Override
	@Transactional
	public InstanceBoundary updateInstanceAccordingRole(String instanceDomain, String instanceId, String userDomain,
			String userEmail, InstanceBoundary input) {
		Optional<UserEntity> userEntityOp = this.userDao.findById(instanceIdToEntity(userDomain, userEmail));

		if (userEntityOp.isPresent()) {
			String role = userEntityOp.get().getRole();
			if (role.equals(UserRole.MANAGER.name()))
				return updateInstanceDB(instanceDomain, instanceId, input);
			else
				throw new RuntimeException("You don't have permission to update information.");
		} else
			throw new RuntimeException("there isn't user with these details");
	}

	@Transactional
	private InstanceBoundary updateInstanceDB(String instanceDomain, String instanceId, InstanceBoundary update) {

		if ((update.getInstanceId() != null && ((update.getInstanceId().getDomain() != null
				&& !instanceDomain.contentEquals(update.getInstanceId().getDomain()))
				|| (update.getInstanceId().getId() != null && !instanceId.contentEquals(update.getInstanceId().getId()))
				|| (update.getCreatedTimestamp() != null))))
			throw new RuntimeException("Cannot update Instance");

		Optional<InstanceEntity> instanceEntity = this.instancesDao
				.findById(instanceIdToEntity(instanceDomain, instanceId));
		if (instanceEntity.isPresent()) {
			if (update.getType() != null)
				instanceEntity.get().setType(update.getType());
			if (update.getName() != null)
				instanceEntity.get().setName(update.getName());
			if (update.getActive() != null)
				instanceEntity.get().setActive(update.getActive());
			if (update.getLocation() != null) {
				if (update.getLocation().getLat() != null)
					instanceEntity.get().setLat(update.getLocation().getLat());
				if (update.getLocation().getLng() != null)
					instanceEntity.get().setLng(update.getLocation().getLng());
			}
			if (update.getInstanceAttributes() != null && update.getInstanceAttributes().size() > 0)
				try {
					instanceEntity.get()
							.setInstanceAttributes(this.jackson.writeValueAsString(update.getInstanceAttributes()));
				} catch (JsonProcessingException e) {
					throw new RuntimeException("Could not write instance attributes as string!");
				}

			return this.InstanceEntityToBoundary(instanceEntity.get());
		} else
			throw new RuntimeException("No such instance in the DB!");

	}

	@Override
	@Deprecated
	public InstanceBoundary getSpecificInstance(String instanceDomain, String instanceId) {
		throw new RuntimeException("deprecated , use getSpecificInstanceAccordingRole instead");
	}

	@Override
	@Transactional(readOnly = true)
	public InstanceBoundary getSpecificInstanceAccordingRole(String instanceDomain, String instanceId,
			String userDomain, String userEmail) {

		Optional<UserEntity> userEntityOp = this.userDao.findById(instanceIdToEntity(userDomain, userEmail));
		String role = "";

		if (userEntityOp.isPresent()) {
			role = getUserRole(userDomain, userEmail);

			if (role.equals(UserRole.MANAGER.name())) {
				return getInstanceBoundary(instanceDomain, instanceId);
			} else if (role.equals(UserRole.PLAYER.name())) {
				InstanceEntity instanceEntity = InstanceBoundaryToEntity(
						getInstanceBoundary(instanceDomain, instanceId));

				if (instanceEntity.isActive())
					return InstanceEntityToBoundary(instanceEntity);
				else
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found.");

			} else
				throw new RuntimeException("You don't have permission to get this information.");
		}

		throw new RuntimeException("No Such Instance!\n");
	}

	@Transactional(readOnly = true)
	private InstanceBoundary getInstanceBoundary(String instanceDomain, String instanceId) {

		Optional<InstanceEntity> instanceEntity = this.instancesDao
				.findById(instanceIdToEntity(instanceDomain, instanceId));
		if (instanceEntity.isPresent()) {
			InstanceBoundary instanceBoundary = this.InstanceEntityToBoundary(instanceEntity.get());
			return instanceBoundary;
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found.");
	}

	@Override
	@Deprecated
	public List<InstanceBoundary> getAllInstances() {
		throw new RuntimeException("deprecated , use paginated data retrieval instead");

	}

	@Override
	@Transactional(readOnly = true)
	public List<InstanceBoundary> getAllInstancesAsList(String userDomain, String userEmail, int size, int page) {

		Optional<UserEntity> userEntity = this.userDao.findById(instanceIdToEntity(userDomain, userEmail));

		if (userEntity.isPresent() && userEntity.get().getRole().contentEquals(UserRole.MANAGER.name()))
			return StreamSupport.stream(this.instancesDao
					.findAll(PageRequest.of(page, size, Direction.ASC, "instanceId", "createdTimestamp")).spliterator(),
					false).map(this::InstanceEntityToBoundary).collect(Collectors.toList());
		else if (userEntity.isPresent() && userEntity.get().getRole().contentEquals(UserRole.PLAYER.name()))
			return StreamSupport.stream(this.instancesDao
					.findAllByActive(true, PageRequest.of(page, size, Direction.ASC, "instanceId", "createdTimestamp"))
					.spliterator(), false).map(this::InstanceEntityToBoundary).collect(Collectors.toList());
		else
			throw new RuntimeException("You don't have the right permissions.");
	}

	@Override
	@Deprecated
	public void deleteAllInstances() {
		this.instancesDao.deleteAll();
	}

	@Override
	@Transactional
	public void deleteAllInstances(String userDomain, String userEmail) {

		Optional<UserEntity> userEntityOp = this.userDao.findById(instanceIdToEntity(userDomain, userEmail));
		if (!userEntityOp.isPresent()
				|| (userEntityOp.isPresent() && !userEntityOp.get().getRole().equalsIgnoreCase(UserRole.ADMIN.name())))
			throw new RuntimeException("You don't have admin permission to access this command.");

		this.instancesDao.deleteAll();
	}

	private InstanceEntity InstanceBoundaryToEntity(InstanceBoundary boundary) throws RuntimeException {
		checkVaildBoundary(boundary);

		InstanceEntity instanceEntity = new InstanceEntity();
		instanceEntity.setInstanceId(boundary.getInstanceId().getDomain() + "@@" + boundary.getInstanceId().getId());
		instanceEntity.setType(boundary.getType());
		instanceEntity.setName(boundary.getName());
		instanceEntity.setActive(boundary.getActive());
		instanceEntity.setCreatedTimestamp(boundary.getCreatedTimestamp());
		instanceEntity.setCreatedByEmail(boundary.getCreatedBy().getUserId().getEmail());
		instanceEntity.setCreatedByDomain(boundary.getCreatedBy().getUserId().getDomain());
		instanceEntity.setLat(boundary.getLocation().getLat());
		instanceEntity.setLng(boundary.getLocation().getLng());

		if (boundary.getInstanceAttributes() != null)
			try {
				instanceEntity.setInstanceAttributes(this.jackson.writeValueAsString(boundary.getInstanceAttributes()));
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Could not convert map to json");
			}

		return instanceEntity;
	}

	private InstanceBoundary InstanceEntityToBoundary(InstanceEntity entity) throws RuntimeException {
		InstanceBoundary instanceBoundary = new InstanceBoundary();

		instanceBoundary.setInstanceId(
				new InstanceId(entity.getInstanceId().split("@@")[0], entity.getInstanceId().split("@@")[1]));
		instanceBoundary.setType(entity.getType());
		instanceBoundary.setName(entity.getName());
		instanceBoundary.setActive(entity.isActive());
		instanceBoundary.setCreatedTimestamp(entity.getCreatedTimestamp());
		instanceBoundary
				.setCreatedBy(new CreatedBy(new UserId(entity.getCreatedByEmail(), entity.getCreatedByDomain())));
		instanceBoundary.setLocation(new Location(entity.getLat(), entity.getLng()));

		if (entity.getInstanceAttributes() != null) {
			try {
				Map<String, Object> attributes = this.jackson.readValue(entity.getInstanceAttributes(), Map.class);
				instanceBoundary.setInstanceAttributes(attributes);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return instanceBoundary;
	}

	private void checkVaildBoundary(InstanceBoundary instanceBoundary) throws RuntimeException {

		// TYPE
		if (!(instanceBoundary.getType().trim().length() > 0))
			throw new RuntimeException("Invalid Type");

		// NAME
		if (!(instanceBoundary.getName().trim().length() > 0))
			throw new RuntimeException("Invalid Name");

		// CREATED_BY
		// Optional<UserEntity> createdBy =
		// this.usersDao.findById(instanceBoundary.getInstanceId().getDomain()
		// for now we will check only if its null because it create connection between
		// the JpaUsers & JpaInstance//+ "@@" +
		// instanceBoundary.getInstanceId().getId());

		if (instanceBoundary.getCreatedBy() == null
				|| !(instanceBoundary.getCreatedBy().getUserId().getEmail().trim().length() > 0)
				|| !(instanceBoundary.getCreatedBy().getUserId().getDomain().trim().length() > 0))
			throw new RuntimeException("Invalid createdBy");

		// LOCATION
		if (instanceBoundary.getLocation() == null || instanceBoundary.getLocation().getLat() == null
				|| instanceBoundary.getLocation().getLng() == null)
			throw new RuntimeException("Invalid location");

		// TIMESTAMP
		if (instanceBoundary.getCreatedTimestamp() == null)
			throw new RuntimeException("invalid timestamp");
	}

	@Override
	@Transactional(readOnly = true)
	public List<InstanceBoundary> searchInstancesByType(String type, String userDomain, String userEmail, int size,
			int page) {

		return handlerTypeRequest(type, userDomain, userEmail,
				PageRequest.of(page, size, Direction.ASC, "name", "instanceId", "createdTimestamp"));
	}

	@Override
	@Transactional(readOnly = true)
	public List<InstanceBoundary> searchInstancesByName(String name, String userDomain, String userEmail, int size,
			int page) {

		return handlerNameRequest(name, userDomain, userEmail,
				PageRequest.of(page, size, Direction.ASC, "name", "instanceId", "createdTimestamp"));
	}

	@Override
	@Transactional(readOnly = true)
	public List<InstanceBoundary> searchInstancesByLocation(String lat, String lng, String distance, String userDomain,
			String userEmail, int size, int page) {

		Optional<UserEntity> userEntity = this.userDao.findById(instanceIdToEntity(userDomain, userEmail));

		double Lat = Double.parseDouble(lat);
		double Lng = Double.parseDouble(lng);
		double Distance = Double.parseDouble(distance);

		if (userEntity.isPresent() && userEntity.get().getRole().contentEquals(UserRole.MANAGER.name()))
			return this.instancesDao
					.findAllByLatBetweenAndLngBetween(Lat - Distance, Lat + Distance, Lng - Distance, Lng + Distance,
							PageRequest.of(page, size, Direction.ASC, "lat", "lng", "instanceId"))
					.stream().map(this::InstanceEntityToBoundary).collect(Collectors.toList());
		else if (userEntity.isPresent() && userEntity.get().getRole().contentEquals(UserRole.PLAYER.name()))
			return this.instancesDao
					.findAllByLatBetweenAndLngBetweenAndActive(Lat - Distance, Lat + Distance, Lng - Distance,
							Lng + Distance, true, PageRequest.of(page, size, Direction.ASC, "lat", "lng", "instanceId"))
					.stream().map(this::InstanceEntityToBoundary).collect(Collectors.toList());
		else
			throw new RuntimeException("You don't have the right permissions.");
	}

	private List<InstanceBoundary> handlerTypeRequest(String type, String userDomain, String userEmail,
			PageRequest pageRequest) {

		String role = getUserRole(userDomain, userEmail);
		List<InstanceBoundary> list = new ArrayList<>();

		if (role.equals(UserRole.ADMIN.name()))
			throw new RuntimeException("You don't have permission to get this information.");
		else if (role.equals(UserRole.MANAGER.name()))
			list = getListSearchByType(type, userDomain, userEmail, pageRequest);
		else if (role.equals(UserRole.PLAYER.name()))
			list = getListByTypeAccordingActive(type, userDomain, userEmail, pageRequest);

		return list;
	}

	@Transactional(readOnly = true)
	private List<InstanceBoundary> getListByTypeAccordingActive(String type, String userDomain, String userEmail,
			PageRequest pageRequest) {

		return this.instancesDao.findAllByTypeAndActive(type, true, pageRequest).stream()
				.map(this::InstanceEntityToBoundary).collect(Collectors.toList());

	}

	@Transactional(readOnly = true)
	private List<InstanceBoundary> getListSearchByType(String type, String userDomain, String userEmail,
			PageRequest pageRequest) {

		return this.instancesDao.findAllByType(type, pageRequest).stream().map(this::InstanceEntityToBoundary)
				.collect(Collectors.toList());
	}

	private List<InstanceBoundary> handlerNameRequest(String name, String userDomain, String userEmail,
			PageRequest pageRequest) {

		String role = getUserRole(userDomain, userEmail);
		List<InstanceBoundary> list = new ArrayList<>();

		if (role.equalsIgnoreCase(UserRole.ADMIN.name()))
			throw new RuntimeException("You don't have permission to get this information.");

		else if (role.equalsIgnoreCase(UserRole.MANAGER.name()))
			list = getListSearchByName(name, userDomain, userEmail, pageRequest);
		else if (role.equalsIgnoreCase(UserRole.PLAYER.name()))
			list = getListByNameAccordingActive(name, userDomain, userEmail, pageRequest);

		return list;
	}

	@Transactional(readOnly = true)
	private List<InstanceBoundary> getListByNameAccordingActive(String name, String userDomain, String userEmail,
			PageRequest pageRequest) {

		return this.instancesDao.findAllByNameAndActive(name, true, pageRequest).stream()
				.map(this::InstanceEntityToBoundary).collect(Collectors.toList());

	}

	@Transactional(readOnly = true)
	private String getUserRole(String userDomain, String userEmail) {
		Optional<UserEntity> userEntityOp = this.userDao.findById(instanceIdToEntity(userDomain, userEmail));

		String role = null;
		if (userEntityOp.isPresent())
			role = userEntityOp.get().getRole();
		else
			throw new RuntimeException("Invalid user!");
		return role;
	}

	@Transactional(readOnly = true)
	private List<InstanceBoundary> getListSearchByName(String nameOrType, String userDomain, String userEmail,
			PageRequest pageRequest) {
		return this.instancesDao.findAllByName(nameOrType, pageRequest).stream().map(this::InstanceEntityToBoundary)
				.collect(Collectors.toList());

	}

	private String instanceIdToEntity(String userDomain, String userEmail) {
		return userDomain + "@@" + userEmail;
	}

}
