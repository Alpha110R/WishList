package iob.restapi.controllers;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import iob.logic.ActivitiesService;
import iob.restapi.boundaries.ActivityBoundary;
import iob.restapi.objects.ActivityId;

@RestController
public class ActivityController {
	
	private ActivitiesService activitiesService;
	public static String defaultDomain;
	
	
	@Autowired
	public ActivityController(ActivitiesService activitiesService) {
		super();
		this.activitiesService = activitiesService;
	}
	

	@Value("${spring.application.name:error}")
	public void setDefaultDomain(String domain) {
		defaultDomain = domain;
	}
	
	
	@RequestMapping(
		path = "/iob/activities",
		method = RequestMethod.POST,
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE)
	public Object invoke(@RequestBody ActivityBoundary input) {
		
		ActivityId id = new ActivityId();
		id.setId(UUID.randomUUID().toString());
		id.setDomain(defaultDomain);
		input.setCreatedTimestamp(new Date());
		input.setActivityId(id);

				
		return (ActivityBoundary) this.activitiesService.invokeActivity(input);
	}
	
	@RequestMapping(
			path = "/iob/admin/activities",
			method = RequestMethod.GET)
	public ActivityBoundary[] getAllActivities() {
			List<ActivityBoundary> list = this.activitiesService.getAllActivities();
			return list.toArray(new ActivityBoundary[0]);
	}
	
	@RequestMapping(
		path = "/iob/admin/activities",
		method = RequestMethod.DELETE)
	public void deleteAllActivities() {
		
		this.activitiesService.deleteAllActivities();;
	}
	
}
