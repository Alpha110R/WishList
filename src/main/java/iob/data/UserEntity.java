package iob.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="USER")
public class UserEntity {

	private String username, avatar, role, userId;
	
	public UserEntity() {
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
	@Id
	public String getUserId() {
		return userId;
	}

	public void setUserId(String id) {
		this.userId = id;
	}
	
	
}
