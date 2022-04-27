package iob.restapi.objects;

public class CreatedBy {
	private UserId userId;
	
	public CreatedBy() {
		
	}

	public CreatedBy(UserId userId) {
		super();
		this.userId = userId;
	}

	public UserId getUserId() {
		return userId;
	}

	public void setUserId(UserId userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "createdBy [userID=" + userId + "]";
	}
	
}
