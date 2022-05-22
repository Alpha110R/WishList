package iob.logic;

import java.util.List;

import iob.restapi.boundaries.UserBoundary;

public interface UsersService {

	public UserBoundary createUser(UserBoundary user);
	public UserBoundary login(String userDomain, String userEmail);
	public UserBoundary updateUser(String userDomain, String userEmail, UserBoundary update);
	@Deprecated
	public List<UserBoundary> getAllUsers();
	@Deprecated
	public void deleteAllUsers();
}
