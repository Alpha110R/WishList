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
import org.springframework.web.bind.annotation.RestController;

import iob.logic.InstancesService;
import iob.restapi.boundaries.InstanceBoundary;
import iob.restapi.objects.InstanceId;

@RestController
public class InstanceController {
	
	private InstancesService instancesService;	
	public static String defaultDomain;
	
	
	@Autowired
	public InstanceController(InstancesService instancesService) {
		super();
		this.instancesService = instancesService ;
	}
	
	
	@Value("${spring.application.name:error}")
	public void setDefaultDomain(String domain) {
		defaultDomain = domain;
	}
	
	
	@RequestMapping(
		path = "/iob/instances",
		method = RequestMethod.GET,
		produces = MediaType.APPLICATION_JSON_VALUE)
	public InstanceBoundary[] getAllInstances() {
		return this.instancesService.getAllInstances().toArray(new InstanceBoundary[0]);
	}
	
	
	@RequestMapping(
		path = "/iob/instances/{instanceDomain}/{instanceId}",
		method = RequestMethod.GET,
		produces = MediaType.APPLICATION_JSON_VALUE)
	public InstanceBoundary retrieveInstance(@PathVariable("instanceDomain") String instanceDomain,
						  	   				 @PathVariable("instanceId") String instanceId) {
	
		return this.instancesService.getSpecificInstance(instanceDomain, instanceId);
	}
	
	@RequestMapping(
		path = "/iob/instances",
		method = RequestMethod.POST,
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE)
	public InstanceBoundary createInstance(@RequestBody InstanceBoundary input) {
		input.setInstanceId(new InstanceId(defaultDomain, UUID.randomUUID()+""));
		input.setCreatedTimestamp(new Date());
		
		return this.instancesService.createInstance(input);
	}
	
	@RequestMapping(
		path = "/iob/instances/{instanceDomain}/{instanceId}",
		method = RequestMethod.PUT,
		consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateInstance(@RequestBody InstanceBoundary input,
							   @PathVariable("instanceDomain") String instanceDomain,
							   @PathVariable("instanceId") String instanceId) {
		this.instancesService.updateInstnce(instanceDomain, instanceId, input);
	}
	
	@RequestMapping(
			path = "/iob/admin/instances",
			method = RequestMethod.DELETE)
		public void deleteAll() {
			this.instancesService.deleteAllInstances();
		}
}
