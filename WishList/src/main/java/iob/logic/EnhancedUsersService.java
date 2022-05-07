package iob.logic;

import java.util.List;

import iob.restapi.boundaries.UserBoundary;

public interface EnhancedUsersService extends UsersService {

	List<UserBoundary> getAllUsers(int page, int size, String userDomain, String userEmail);
	void deleteAllUsers(String userDomain, String userEmail);
	
}
