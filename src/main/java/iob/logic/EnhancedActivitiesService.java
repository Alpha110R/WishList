package iob.logic;

import java.util.List;

import iob.restapi.boundaries.ActivityBoundary;

public interface EnhancedActivitiesService extends ActivitiesService {

	public List<ActivityBoundary> getAllActivities(String userDomain, String userEmail, int size, int page);
	public void deleteAllActivities(String userDomain, String userEmail);
}
