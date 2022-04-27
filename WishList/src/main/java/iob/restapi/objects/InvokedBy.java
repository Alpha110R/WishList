package iob.restapi.objects;

public class InvokedBy {
	private UserId userId;
	
	public InvokedBy() {
		
	}

	public InvokedBy(iob.restapi.objects.UserId userId) {
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
		return "invokedBy [userId=" + userId + "]";
	}
	
}
