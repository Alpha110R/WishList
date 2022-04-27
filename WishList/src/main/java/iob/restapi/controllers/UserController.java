package iob.restapi.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import iob.logic.UsersService;
import iob.restapi.boundaries.NewUserBoundary;
import iob.restapi.boundaries.UserBoundary;
import iob.restapi.objects.UserId;


@RestController
public class UserController {
	
	private UsersService usersService;	
	public static String defaultDomain;
	
	@Autowired
	public UserController(UsersService usersService) {
		super();
		this.usersService = usersService;
	}
	
	
	@Value("${spring.application.name:error}")
	public void setDefaultDomain(String domain) {
		defaultDomain = domain;
	}
	
	@RequestMapping(
		path = "/iob/users",
		method = RequestMethod.POST,
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary createUser(@RequestBody NewUserBoundary input) {
		
		UserId userId = new UserId(input.getEmail());
		userId.setDomain(defaultDomain);

		UserBoundary userBoundary = new UserBoundary(userId, 
				input.getUsername(), input.getRole(), input.getAvatar());

		return this.usersService.createUser(userBoundary);
	}
	
	@RequestMapping(
		path = "/iob/users/login/{userDomain}/{userEmail}",
		method = RequestMethod.GET,
		produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary login(@PathVariable("userDomain") String userDomain,
						  	   @PathVariable("userEmail") String userEmail) {
		return this.usersService.login(userDomain, userEmail);
	}
	
	@RequestMapping(
		path = "/iob/users/{userDomain}/{userEmail}",
		method = RequestMethod.PUT,
		consumes = MediaType.APPLICATION_JSON_VALUE)
	public void update(@RequestBody UserBoundary input,
			@PathVariable("userDomain") String userDomain,
			@PathVariable("userEmail") String userEmail) {
		this.usersService.updateUser(userDomain, userEmail, input);
		
	}
	
	@RequestMapping(
		path = "/iob/admin/users",
		method = RequestMethod.GET)
	public UserBoundary[] getAllUsers() {
		List<UserBoundary> list = this.usersService.getAllUsers();
		return list.toArray(new UserBoundary[0]);
	}
	
	@RequestMapping(
		path = "/iob/admin/users",
		method = RequestMethod.DELETE)
	public void deleteAll() {
		this.usersService.deleteAllUsers();
	}

}
