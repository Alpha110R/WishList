package iob.restapi.objects;

public class UserId {
	
	private String email;
	private String domain;
	public UserId() {
	}

	public UserId(String email) {
		super();
		this.email = email;
	}
	
	public UserId(String email, String domain) {
		super();
		this.email = email;
		this.domain = domain;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Override
	public String toString() {
		return "UserId [domain=" + domain + ", email=" + email + "]";
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
