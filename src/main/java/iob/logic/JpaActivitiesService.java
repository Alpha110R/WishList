package iob.logic;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import iob.data.ActivityDao;
import iob.data.ActivityEntity;
import iob.data.InstanceEntity;
import iob.data.InstancesDao;
import iob.data.UserDao;
import iob.data.UserEntity;
import iob.data.UserRole;
import iob.restapi.boundaries.ActivityBoundary;
import iob.restapi.objects.ActivityId;
import iob.restapi.objects.Instance;
import iob.restapi.objects.InstanceId;
import iob.restapi.objects.InvokedBy;
import iob.restapi.objects.UserId;

@Service
public class JpaActivitiesService implements EnhancedActivitiesService {

	private ActivityDao activityDao;
	private UserDao userDao;
	private InstancesDao instancesDao;
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	public JpaActivitiesService(ActivityDao activityDao, UserDao userDao, InstancesDao instancesDao) {
		this.activityDao = activityDao;
		this.userDao = userDao;
		this.instancesDao = instancesDao;
	}

	@Override
	@Transactional
	public Object invokeActivity(ActivityBoundary activity) {

		if (activity == null) {
			return null;
		}

		// Type not null and type not empty
		if (activity.getType() == null || !(activity.getType().trim().length() > 0)) {
			throw new RuntimeException("Invalid Type");
		}

		// Is valid userId
		Optional<UserEntity> userOptional = this.userDao.findById(activity.getInvokedBy().getUserId().getDomain() + "@@"
				+ activity.getInvokedBy().getUserId().getEmail());
		if (!userOptional.isPresent()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

		}

		// Is valid user role
		if (!userOptional.get().getRole().equalsIgnoreCase(UserRole.PLAYER.name())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid user role");
		}

		// Is valid instanceId
		Optional<InstanceEntity> instanceOptional = this.instancesDao
				.findById(activity.getInstance().getInstanceId().getDomain() + "@@"
						+ activity.getInstance().getInstanceId().getId());
		if (!instanceOptional.isPresent()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instnace not found");
		}

		// Is active instance
		if (!instanceOptional.get().isActive()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instnace not active");
		}

		try {
			ActivityEntity entity = this.ActivityBoundaryToEntity(activity);
			entity = this.activityDao.save(entity);
			return this.ActivityEntityToBoundary(entity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@Deprecated
	public List<ActivityBoundary> getAllActivities() {
		throw new RuntimeException("Deprecated operation");
	}

	@Override
	@Transactional(readOnly = true)
	public List<ActivityBoundary> getAllActivities(String userDomain, String userEmail, int size, int page) {

		// Is valid userId
		Optional<UserEntity> userOptional = this.userDao.findById(userDomain + "@@" + userEmail);
		if (!userOptional.isPresent()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		// Is valid user role
		if (!userOptional.get().getRole().equalsIgnoreCase(UserRole.ADMIN.name())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid user role");
		}

		return this.activityDao.findAll(PageRequest.of(page, size, Direction.ASC, "createdTimestamp", "activityId")) // Page<ActivityEntity>
				.getContent() // List<ActivityEntity>
				.stream() // Stream<ActivityEntity>
				.map(this::ActivityEntityToBoundary) // Stream<ActivityBoundary>
				.collect(Collectors.toList()); // List<ActivityBoundary>
	}

	@Override
	@Deprecated
	public void deleteAllActivities() {
		throw new RuntimeException("Deprecated operation");
	}

	@Override
	@Transactional
	public void deleteAllActivities(String userDomain, String userEmail) {

		// Is valid userId
		Optional<UserEntity> userOptional = this.userDao.findById(userDomain + "@@" + userEmail);
		if (!userOptional.isPresent()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		// Is valid user role
		if (!userOptional.get().getRole().equalsIgnoreCase(UserRole.ADMIN.name())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid user role");
		}
		this.activityDao.deleteAll();

	}

	private ActivityEntity ActivityBoundaryToEntity(ActivityBoundary boundary) {
		ActivityEntity entity = new ActivityEntity();

		// ACTIVITY ID
		entity.setActivityId(boundary.getActivityId().getDomain() + "@@" + boundary.getActivityId().getId());

		// TYPE
		if (boundary.getType() != null) {
			entity.setType(boundary.getType());
		}

		// INSTANCE
		if (boundary.getInstance() != null) {
			entity.setInstance(boundary.getInstance().getInstanceId().getDomain() + "@@"
					+ String.valueOf(boundary.getInstance().getInstanceId().getId()));
		}

		// TIMESTAMP
		entity.setCreatedTimestamp(boundary.getCreatedTimestamp());

		// INVOKED BY
		if (boundary.getInvokedBy() != null) {
			if (boundary.getInvokedBy().getUserId() != null) {
				if (boundary.getInvokedBy().getUserId().getEmail() != null) {
					entity.setInvokedBy(boundary.getInvokedBy().getUserId().getDomain() + "@@"
							+ boundary.getInvokedBy().getUserId().getEmail());
				}
			}
		}

		// ACTIVITY ATTRIBUTES
		Map<String, Object> map = boundary.getActivityAttributes();
		if (map != null) {
			try {
				String json = this.mapper.writeValueAsString(map);

				entity.setActivityAttributes(json);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			entity.setActivityAttributes(null);
		}

		return entity;

	}

	private ActivityBoundary ActivityEntityToBoundary(ActivityEntity entity) {
		ActivityBoundary boundary = new ActivityBoundary();

		// ACTIVITY ID
		String id[] = entity.getActivityId().split("@@");
		ActivityId aid = new ActivityId();
		aid.setDomain(id[0]);
		aid.setId(id[1]);
		boundary.setActivityId(aid);

		// ACTIVITY TYPE
		if (entity.getType() != null) {
			boundary.setType(entity.getType());
		}

		// INSTANCE
		if (entity.getInstance() != null) {
			String instanceId[] = entity.getInstance().split("@@");
			Instance ins = new Instance();
			InstanceId inId = new InstanceId();
			inId.setDomain(instanceId[0]);
			inId.setId((instanceId[1]));
			ins.setInstanceId(inId);
			boundary.setInstance(ins);
		}

		// TIMPESTAMP
		boundary.setCreatedTimestamp(entity.getCreatedTimestamp());

		// INVOKED BY
		if (entity.getInvokedBy() != null) {
			String[] invokedByString = entity.getInvokedBy().split("@@");
			InvokedBy invokedBy = new InvokedBy();
			UserId userId = new UserId();
			userId.setDomain(invokedByString[0]);
			userId.setEmail(invokedByString[1]);
			invokedBy.setUserId(userId);
			boundary.setInvokedBy(invokedBy);
		}

		// ACTIVITY ATTRIBUTES
		Map<String, Object> activityAttributes = null;
		if (entity.getActivityAttributes() != null) {
			try {
				activityAttributes = this.mapper.readValue(entity.getActivityAttributes(), Map.class);
				boundary.setActivityAttributes(activityAttributes);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			boundary.setActivityAttributes(null);
		}

		return boundary;

	}

}
