package iob.logic;

import java.util.List;

import iob.restapi.boundaries.InstanceBoundary;

public interface InstancesService {
	public InstanceBoundary createInstance(InstanceBoundary instance);
	public InstanceBoundary updateInstnce(String instanceDomain, String instanceId, InstanceBoundary update);
	public InstanceBoundary getSpecificInstance(String instanceDomain, String instanceId);
	public List <InstanceBoundary> getAllInstances();
	public void deleteAllInstances();
}
