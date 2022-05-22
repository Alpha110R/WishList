package iob.restapi.objects;

public class InstanceId {
	private String domain, id;
	
	public InstanceId() {
	
	}

	public InstanceId(String domain, String id) {
		super();
		this.domain = domain;
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "instanceId [domain=" + domain + ", id=" + id + "]";
	}
	
}
