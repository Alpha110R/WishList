package iob.logic;

import java.util.List;

import iob.restapi.boundaries.InstanceBoundary;

public interface InstancesService {
	public InstanceBoundary createInstance(InstanceBoundary instance);
	@Deprecated
	public InstanceBoundary updateInstnce(String instanceDomain, String instanceId, InstanceBoundary update);
	@Deprecated
	public InstanceBoundary getSpecificInstance(String instanceDomain, String instanceId);
	@Deprecated
	public List <InstanceBoundary> getAllInstances();
	
	public void deleteAllInstances();
}
