package iob.restapi.objects;

public class Instance {
	private InstanceId instanceId;
	
	public Instance() {
		
	}

	public Instance(iob.restapi.objects.InstanceId instanceId, Location location) {
		super();
		this.instanceId = instanceId;
	}

	public InstanceId getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(InstanceId instanceId) {
		this.instanceId = instanceId;
	}

	@Override
	public String toString() {
		return "Instance [instanceId=" + instanceId + "]";
	}
	
	
}
