package iob.restapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iob.logic.EnhancedInstancesService;
import iob.restapi.boundaries.InstanceBoundary;

@RestController
public class InstanceSearchController {

	private EnhancedInstancesService instancesService;

	@Autowired
	public InstanceSearchController(EnhancedInstancesService instancesService) {
		super();
		this.instancesService = instancesService;
	}

	@RequestMapping(path = "/iob/instances/search/ByName/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public InstanceBoundary[] newSearchInstancesByName(@PathVariable("name") String name,
			@RequestParam(name = "userDomain", required = true) String userDomain,
			@RequestParam(name = "userEmail", required = true) String userEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {

		return this.instancesService.searchInstancesByName(name, userDomain, userEmail, size, page)
				.toArray(new InstanceBoundary[0]);
	}

	@RequestMapping(path = "/iob/instances/search/ByType/{type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public InstanceBoundary[] newSearchInstancesByType(@PathVariable("type") String type,
			@RequestParam(name = "userDomain", required = true) String userDomain,
			@RequestParam(name = "userEmail", required = true) String userEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {

		return this.instancesService.searchInstancesByType(type, userDomain, userEmail, size, page)
				.toArray(new InstanceBoundary[0]);
	}

	@RequestMapping(path = "/iob/instances/search/near/{lat}/{lng}/{distance}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public InstanceBoundary[] newSearchInstancesByLocation(@PathVariable("lat") String lat,
			@PathVariable("lng") String lng, @PathVariable("distance") String distance,
			@RequestParam(name = "userDomain", required = true) String userDomain,
			@RequestParam(name = "userEmail", required = true) String userEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {

		return this.instancesService.searchInstancesByLocation(lat, lng, distance, userDomain, userEmail, size, page)
				.toArray(new InstanceBoundary[0]);
	}

}
