package iob.logic;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iob.data.InstanceEntity;
import iob.data.InstancesDao;
import iob.restapi.boundaries.InstanceBoundary;
import iob.restapi.objects.CreatedBy;
import iob.restapi.objects.InstanceId;
import iob.restapi.objects.Location;
import iob.restapi.objects.UserId;


@Service
public class JpaInstancesService implements InstancesService {

	private InstancesDao instancesDao ;
	private ObjectMapper jackson;
	
	
	@Autowired
	public JpaInstancesService(InstancesDao instancesDao) {
		this.instancesDao = instancesDao;
	}
	
	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}
	
	@Override
	public InstanceBoundary createInstance(InstanceBoundary instance) {
		InstanceEntity instanceEntity = this.InstanceBoundaryToEntity(instance);
		if(instanceEntity  != null) {
			instanceEntity = this.instancesDao.save(instanceEntity);
			return this.InstanceEntityToBoundary(instanceEntity);
		}
		return null;
		
	}
	
	@Override
	@Transactional
	public InstanceBoundary updateInstnce(String instanceDomain, String instanceId, InstanceBoundary update) {
		
		if ((update.getInstanceId() != null && ((update.getInstanceId().getDomain() != null && !instanceDomain.contentEquals(update.getInstanceId().getDomain()))
				|| (update.getInstanceId().getId() != null && !instanceId.contentEquals(update.getInstanceId().getId()))
				|| (update.getCreatedTimestamp() != null))))
			throw new RuntimeException("Cannot update Instance");
		
		Optional<InstanceEntity> instanceEntity = this.instancesDao.findById(instanceDomain + "@@" + instanceId);
		if(instanceEntity.isPresent()) {
			if(update.getType() != null)
				instanceEntity.get().setType(update.getType());
			if(update.getName() != null)
				instanceEntity.get().setName(update.getName());
			if(update.getActive() != null)
				instanceEntity.get().setActive(update.getActive());
			if(update.getLocation() != null) {
				if(update.getLocation().getLat() != null)
					instanceEntity.get().setLat(update.getLocation().getLat());
				if(update.getLocation().getLng() != null)
					instanceEntity.get().setLng(update.getLocation().getLng());
			}
			return this.InstanceEntityToBoundary(instanceEntity.get());
		}
		else {
			throw new RuntimeException("Failed to update Instance!");
		}
	}
	
	@Override
	public InstanceBoundary getSpecificInstance(String instanceDomain, String instanceId) {
		Optional<InstanceEntity> instanceEntity = this.instancesDao.findById(instanceDomain + "@@" + instanceId);
		if(instanceEntity.isPresent()) {
			
			InstanceBoundary instanceBoundary = this.InstanceEntityToBoundary(instanceEntity.get());
			return instanceBoundary;
		}else {
			throw new RuntimeException("No Such Instance!\n");
		}	
	}

	@Override
	@Transactional
	public List<InstanceBoundary> getAllInstances() {
			return StreamSupport.stream(this.instancesDao.findAll()
					.spliterator(), false)
					.map(this::InstanceEntityToBoundary)
					.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void deleteAllInstances() {
		this.instancesDao.deleteAll();
	}

	public InstanceEntity InstanceBoundaryToEntity(InstanceBoundary boundary) throws RuntimeException {
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
		
		if(boundary.getInstanceAttributes() != null)
			try {
				instanceEntity.setInstanceAttributes(this.jackson.writeValueAsString(boundary.getInstanceAttributes()));
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Could not convert map to json");
			}
		
		return instanceEntity;
	}
	
	public InstanceBoundary InstanceEntityToBoundary(InstanceEntity entity) throws RuntimeException {
		InstanceBoundary instanceBoundary = new InstanceBoundary();
		
		instanceBoundary.setInstanceId(new InstanceId(entity.getInstanceId().split("@@")[0], entity.getInstanceId().split("@@")[1]));
		instanceBoundary.setType(entity.getType());
		instanceBoundary.setName(entity.getName());
		instanceBoundary.setActive(entity.isActive());
		instanceBoundary.setCreatedTimestamp(entity.getCreatedTimestamp());
		instanceBoundary.setCreatedBy(new CreatedBy(new UserId(entity.getCreatedByEmail(), entity.getCreatedByDomain())));
		instanceBoundary.setLocation(new Location(entity.getLat(), entity.getLng()));
		
		if(entity.getInstanceAttributes() != null) {
			try {
				Map<String, Object> attributes = this.jackson.readValue(entity.getInstanceAttributes(), Map.class);
				instanceBoundary.setInstanceAttributes(attributes);
			}catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return instanceBoundary;
	}
	
	public void checkVaildBoundary(InstanceBoundary instanceBoundary) throws RuntimeException {
		
		//TYPE
		if(!(instanceBoundary.getType().trim().length() > 0))
			throw new RuntimeException("Invalid Type");
		
		//NAME
		if(!(instanceBoundary.getName().trim().length() > 0))
			throw new RuntimeException("Invalid Name");
		
		//CREATED_BY
		//Optional<UserEntity> createdBy = this.usersDao.findById(instanceBoundary.getInstanceId().getDomain()
		//for now we will check only if its null because it create connection between the JpaUsers & JpaInstance//+ "@@" + instanceBoundary.getInstanceId().getId());
		
		if(instanceBoundary.getCreatedBy() == null ||
		   !(instanceBoundary.getCreatedBy().getUserId().getEmail().trim().length() > 0) ||
		   !(instanceBoundary.getCreatedBy().getUserId().getDomain().trim().length() > 0))
			throw new RuntimeException("Invalid createdBy");
		
		//LOCATION
		if(instanceBoundary.getLocation() == null || 
		   instanceBoundary.getLocation().getLat() == null || 
		   instanceBoundary.getLocation().getLng() == null)
			throw new RuntimeException("Invalid location");
		
		//TIMESTAMP
		if(instanceBoundary.getCreatedTimestamp() == null)
			throw new RuntimeException("invalid timestamp");	
	}
	
}
