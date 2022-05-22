package iob.logic;

import java.util.List;

import iob.restapi.boundaries.InstanceBoundary;

public interface EnhancedInstancesService extends InstancesService {

	public  List <InstanceBoundary> getAllInstancesAsList(String userDomain, String userEmail, int size, int page);

	public List <InstanceBoundary> searchInstancesByName(String name, String userDomain, String userEmail, int size, int page);

	public List<InstanceBoundary> searchInstancesByType(String type, String userDomain, String userEmail, int size,int page);

	public InstanceBoundary updateInstanceAccordingRole(String instanceDomain, String instanceId, String userDomain,String userEmail, InstanceBoundary input);

	public InstanceBoundary getSpecificInstanceAccordingRole(String instanceDomain, String instanceId,String userDomain, String userEmail);

	public List<InstanceBoundary> searchInstancesByLocation(String lat, String lng, String distance, String userDomain, String userEmail, int size, int page);

	void deleteAllInstances(String userDomain, String userEmail);

}
