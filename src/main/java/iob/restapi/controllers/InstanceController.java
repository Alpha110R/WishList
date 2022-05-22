package iob.restapi.controllers;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iob.logic.EnhancedInstancesService;
import iob.restapi.boundaries.InstanceBoundary;
import iob.restapi.objects.InstanceId;

@RestController
public class InstanceController {

	private EnhancedInstancesService instancesService;
	public static String defaultDomain;

	@Autowired
	public InstanceController(EnhancedInstancesService instancesService) {
		super();
		this.instancesService = instancesService;
	}

	@Value("${spring.application.name:error}")
	public void setDefaultDomain(String domain) {
		defaultDomain = domain;
	}

	@RequestMapping(path = "/iob/instances", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public InstanceBoundary[] getAllInstances(@RequestParam(name = "userDomain", required = true) String userDomain,
			@RequestParam(name = "userEmail", required = true) String userEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.instancesService.getAllInstancesAsList(userDomain, userEmail, size, page)
				.toArray(new InstanceBoundary[0]);
	}

	@RequestMapping(path = "/iob/instances/{instanceDomain}/{instanceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public InstanceBoundary retrieveInstance(@PathVariable("instanceDomain") String instanceDomain,
			@PathVariable("instanceId") String instanceId,
			@RequestParam(name = "userDomain", required = true) String userDomain,
			@RequestParam(name = "userEmail", required = true) String userEmail) {

		return this.instancesService.getSpecificInstanceAccordingRole(instanceDomain, instanceId, userDomain,
				userEmail);
	}

	@RequestMapping(path = "/iob/instances", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public InstanceBoundary createInstance(@RequestBody InstanceBoundary input) {
		input.setInstanceId(new InstanceId(defaultDomain, UUID.randomUUID() + ""));
		input.setCreatedTimestamp(new Date());

		return this.instancesService.createInstance(input);
	}

	@RequestMapping(path = "/iob/instances/{instanceDomain}/{instanceId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateInstance(@RequestBody InstanceBoundary input,
			@PathVariable("instanceDomain") String instanceDomain, @PathVariable("instanceId") String instanceId,
			@RequestParam(name = "userDomain", required = true) String userDomain,
			@RequestParam(name = "userEmail", required = true) String userEmail) {
		this.instancesService.updateInstanceAccordingRole(instanceDomain, instanceId, userDomain, userEmail, input);
	}

	@RequestMapping(path = "/iob/admin/instances", method = RequestMethod.DELETE)
	public void deleteAll(@RequestParam(name = "userDomain", required = true) String userDomain,
			@RequestParam(name = "userEmail", required = true) String userEmail) {
		this.instancesService.deleteAllInstances(userDomain, userEmail);
	}

}
