package iob.logic;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iob.data.UserDao;
import iob.data.UserEntity;
import iob.data.UserRole;
import iob.restapi.boundaries.UserBoundary;
import iob.restapi.controllers.UserController;
import iob.restapi.objects.UserId;

@Service
public class JpaUsersService implements EnhancedUsersService {
	private UserDao userDao;

	@Autowired
	public JpaUsersService(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	@Transactional
	public UserBoundary createUser(UserBoundary input) {

		UserEntity userEntity = this.UserBoundaryToEntity(input);
		if (userEntity != null) {
			userEntity = this.userDao.save(userEntity);
			return this.UserEntityToBoundary(userEntity);
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public UserBoundary login(String userDomain, String userEmail) {
		Optional<UserEntity> userEntityOp = this.userDao.findById(UserIdToEntity(userDomain, userEmail));
		if (userEntityOp.isPresent()) {
			UserEntity userEntity = userEntityOp.get();
			return this.UserEntityToBoundary(userEntity);
		} else {
			throw new RuntimeException("No such user with Domain: " + userDomain + " Email: " + userEmail);
		}
	}

	@Override
	@Transactional
	public UserBoundary updateUser(String userDomain, String userEmail, UserBoundary update) {

		if (update.getUserId() != null)
			if ((update.getUserId().getDomain() != null && !userDomain.contentEquals(update.getUserId().getDomain()))
					|| (update.getUserId().getEmail() != null
							&& !userEmail.contentEquals(update.getUserId().getEmail())))
				throw new RuntimeException("Cannot update domain or email");

		Optional<UserEntity> userEntityOp = this.userDao.findById(UserIdToEntity(userDomain, userEmail));
		if (userEntityOp.isPresent()) {
			UserEntity userEntity = userEntityOp.get();

			if (update.getRole() != null)
				userEntity.setRole(update.getRole());

			if (update.getUsername() != null)
				userEntity.setUsername(update.getUsername());

			if (update.getAvatar() != null)
				userEntity.setAvatar(update.getAvatar());

			userEntity = this.userDao.save(userEntity);
			return this.UserEntityToBoundary(userEntity);
		} else {
			throw new RuntimeException("Failed to update user!\n");
		}
	}

	@Override
	@Deprecated
	public List<UserBoundary> getAllUsers() {
		throw new RuntimeException("Deprecated getAllUsers");
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBoundary> getAllUsers(int page, int size, String userDomain, String userEmail) {

		Optional<UserEntity> userEntityOp = this.userDao.findById(UserIdToEntity(userDomain, userEmail));

		if (!userEntityOp.isPresent()
				|| (userEntityOp.isPresent() && !userEntityOp.get().getRole().equalsIgnoreCase(UserRole.ADMIN.name())))
			throw new RuntimeException("You don't have admin permission to access this command.");

		return this.userDao.findAll(PageRequest.of(page, size, Direction.ASC, "username", "userId")).getContent()
				.stream().map(this::UserEntityToBoundary).collect(Collectors.toList());
	}

	@Override
	@Deprecated
	public void deleteAllUsers() {
		throw new RuntimeException("Deprecated deleteAllUsers");
	}

	@Override
	@Transactional
	public void deleteAllUsers(String userDomain, String userEmail) {

		Optional<UserEntity> userEntityOp = this.userDao.findById(UserIdToEntity(userDomain, userEmail));
		if (!userEntityOp.isPresent()
				|| (userEntityOp.isPresent() && !userEntityOp.get().getRole().equalsIgnoreCase(UserRole.ADMIN.name())))
			throw new RuntimeException("You don't have admin permission to access this command.");

		this.userDao.deleteAll();
	}

	private UserEntity UserBoundaryToEntity(UserBoundary boundary) throws RuntimeException {
		checkVaildBoundary(boundary);
		UserEntity userEntity = new UserEntity();
		userEntity.setUserId(UserIdToEntity(boundary.getUserId().getDomain(), boundary.getUserId().getEmail()));
		userEntity.setRole(boundary.getRole().toUpperCase());
		userEntity.setUsername(boundary.getUsername());
		userEntity.setAvatar(boundary.getAvatar());

		return userEntity;
	}

	private UserBoundary UserEntityToBoundary(UserEntity entity) throws RuntimeException {
		UserBoundary userBoundary = new UserBoundary();
		userBoundary.setUserId(UserIdToBoundary(entity.getUserId()));
		userBoundary.setRole(entity.getRole().toUpperCase());
		userBoundary.setUsername(entity.getUsername());
		userBoundary.setAvatar(entity.getAvatar());

		checkVaildBoundary(userBoundary);

		return userBoundary;
	}

	private String UserIdToEntity(String userDomain, String userEmail) {
		return userDomain + "@@" + userEmail;
	}

	private UserId UserIdToBoundary(String userId) {
		return new UserId(userId.split("@@")[1], userId.split("@@")[0]);
	}

	/***
	 * Check if all the variables of the passed UserBoundary are valid.
	 * 
	 * @param userBoundary
	 * @throws RuntimeException
	 */
	private void checkVaildBoundary(UserBoundary userBoundary) throws RuntimeException {

		// DOMAIN
		if (!userBoundary.getUserId().getDomain().contentEquals(UserController.defaultDomain))
			throw new RuntimeException("Invalid domain");

		// EMAIL
		if (!isValidEmailAddress(userBoundary.getUserId().getEmail()))
			throw new RuntimeException("Invalid email");

		// USERNAME
		if (userBoundary.getUsername() == null || userBoundary.getUsername().trim().length() == 0)
			throw new RuntimeException("Username cannot be null or empty");

		// AVATAR
		if (userBoundary.getAvatar() == null || userBoundary.getAvatar().trim().length() == 0)
			throw new RuntimeException("Avatar cannot be null or empty");

		// ROLE
		boolean found = false;
		for (UserRole role : UserRole.values())
			if (role.name().contentEquals(userBoundary.getRole().toUpperCase()))
				found = true;

		if (!found)
			throw new RuntimeException("Invalid role");

	}

	private boolean isValidEmailAddress(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\"
				+ ".[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\" + ".)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}

}
