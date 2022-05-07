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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iob.logic.EnhancedActivitiesService;
import iob.restapi.boundaries.ActivityBoundary;
import iob.restapi.objects.ActivityId;

@RestController
public class ActivityController {

	private EnhancedActivitiesService activitiesService;
	public static String defaultDomain;

	@Autowired
	public ActivityController(EnhancedActivitiesService activitiesService) {
		super();
		this.activitiesService = activitiesService;
	}

	@Value("${spring.application.name:error}")
	public void setDefaultDomain(String domain) {
		defaultDomain = domain;
	}

	@RequestMapping(path = "/iob/activities", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Object invoke(@RequestBody ActivityBoundary input) {

		ActivityId id = new ActivityId();
		id.setId(UUID.randomUUID().toString());
		id.setDomain(defaultDomain);
		input.setCreatedTimestamp(new Date());
		input.setActivityId(id);

		return (ActivityBoundary) this.activitiesService.invokeActivity(input);
	}

	@RequestMapping(path = "/iob/admin/activities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ActivityBoundary[] getAllActivities(@RequestParam(name = "userDomain", required = true) String userDomain,
			@RequestParam(name = "userEmail", required = true) String userEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {

		List<ActivityBoundary> list = this.activitiesService.getAllActivities(userDomain, userEmail, size, page);

		return list.toArray(new ActivityBoundary[0]);
	}

	@RequestMapping(path = "/iob/admin/activities", method = RequestMethod.DELETE)
	public void deleteAllActivities(@RequestParam(name = "userDomain", required = true) String userDomain,
			@RequestParam(name = "userEmail", required = true) String userEmail) {
		this.activitiesService.deleteAllActivities(userDomain, userEmail);
	}

}
