package iob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import iob.restapi.boundaries.ActivityBoundary;
import iob.restapi.boundaries.InstanceBoundary;
import iob.restapi.boundaries.NewUserBoundary;
import iob.restapi.boundaries.UserBoundary;
import iob.restapi.objects.ActivityId;
import iob.restapi.objects.CreatedBy;
import iob.restapi.objects.Instance;
import iob.restapi.objects.InstanceId;
import iob.restapi.objects.InvokedBy;
import iob.restapi.objects.Location;
import iob.restapi.objects.UserId;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ActivityTests {

	private int port;
	private String url;
	private String domain = "2022b.timor.bystritskie";
	private RestTemplate restTemplate; // reference to a helper object that invokes REST API

	@LocalServerPort
	public void setPort(int port) {
		this.port = port;
	}

	@PostConstruct
	public void init() {
		this.restTemplate = new RestTemplate();
		this.url = "http://localhost:" + this.port + "/iob/";
	}

	@DisplayName("Validate that only players can invoke activities")
	@Test
	public void testInvokeActivity() {

//		GIVEN the server IP is up and there are valid PLAYER user in the DB and ADMIN user in the DB and valid MANAGER user in the DB,
//		AND there is a valid Instance in the DB,

		NewUserBoundary user1 = new NewUserBoundary("test0@domain.com", "PLAYER", "test1", "808");
		NewUserBoundary user2 = new NewUserBoundary("test1@domain.com", "ADMIN", "test2", "8082");
		NewUserBoundary user3 = new NewUserBoundary("test2@domain.com", "MANAGER", "test2", "8082");

		this.restTemplate.postForObject(this.url + "/users/", user1, UserBoundary.class);
		this.restTemplate.postForObject(this.url + "/users/", user2, UserBoundary.class);
		this.restTemplate.postForObject(this.url + "/users/", user3, UserBoundary.class);

		InstanceBoundary instanceBoundary = new InstanceBoundary(new InstanceId(domain, "1"), // instance id
				"drmotype", // instance type
				"testInstance", // instance name
				true, // is active
				null, // time stamp
				new CreatedBy(new UserId("test1@domain.com", domain)), // created by
				new Location(0.0, 0.0), // location
				new HashMap<String, Object>()); // instanceAttributes

		InstanceBoundary instance = this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary,
				InstanceBoundary.class);

		ActivityBoundary activityBoundary1 = new ActivityBoundary(new ActivityId(domain, "i") // activity id
				, "demotype" // activity type
				, new Instance(instance.getInstanceId(), instance.getLocation()) // Instance
				, null // time stamp
				, new InvokedBy(new UserId("test0@domain.com", domain)) // invoked by
				, null); // activity attributes

		ActivityBoundary activityBoundary2 = new ActivityBoundary(new ActivityId(domain, "i") // activity id
				, "demotype" // activity type
				, new Instance(instance.getInstanceId(), instance.getLocation()) // Instance
				, null // time stamp
				, new InvokedBy(new UserId("test1@domain.com", domain)) // invoked by
				, null); // activity attributes

		new ActivityBoundary(new ActivityId(domain, "i") // activity id
				, "demotype" // activity type
				, new Instance(instance.getInstanceId(), instance.getLocation()) // Instance
				, null // time stamp
				, new InvokedBy(new UserId("test2@domain.com", domain)) // invoked by
				, null); // activity attributes

		ActivityBoundary invokedActivityBoundary1 = this.restTemplate.postForObject(this.url + "/activities",
				activityBoundary1, ActivityBoundary.class);

//		WHEN I POST /iob/activities with PLAYER user, with valid id
//		THEN the server will respond with object jSON
//		AND the activity is not null
//		AND the response activity id is not null
		assertThat(invokedActivityBoundary1).isNotNull();

		assertThat(invokedActivityBoundary1.getActivityId()).isNotNull();

		assertThat(invokedActivityBoundary1.getActivityId().getId()).isNotNull();

//		WHEN I POST /iob/activities with ADMIN user, with valid id
//		THEN the server will respond with RuntimeException

		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.postForObject(this.url + "/activities", activityBoundary2, ActivityBoundary.class);
		});

//		WHEN I POST /iob/activities with MANAGER user, with valid id
//		THEN the server will respond with RuntimeException
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.postForObject(this.url + "/activities", activityBoundary2, ActivityBoundary.class);
		});

	}

	@DisplayName("Validate that only admins can get activities lists")
	@Test
	public void testGetAllActivities() {

//		GIVEN the server IP is up and there are valid PLAYER and ADMIN users in the DB,
//		AND there are valid INstance in the DB,
//		AND there are 15 activities in the DB

		NewUserBoundary user1 = new NewUserBoundary("test1@domain.com", "PLAYER", "test1", "808");
		NewUserBoundary user2 = new NewUserBoundary("test0@domain.com", "ADMIN", "test2", "8082");

		this.restTemplate.postForObject(this.url + "/users/", user1, UserBoundary.class);
		this.restTemplate.postForObject(this.url + "/users/", user2, UserBoundary.class);

		InstanceBoundary instanceBoundary = new InstanceBoundary(new InstanceId(domain, "1"), // instance id
				"drmotype", // instance type
				"testInstance", // instance name
				true, // is active
				null, // time stamp
				new CreatedBy(new UserId("test1@domain.com", domain)), // created by
				new Location(0.0, 0.0), // location
				new HashMap<String, Object>()); // instanceAttributes

		InstanceBoundary instance = this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary,
				InstanceBoundary.class);

		List<ActivityBoundary> lst = new ArrayList<>();
		for (int i = 0; i < 15; i++) {

			ActivityBoundary activityBoundary = new ActivityBoundary(new ActivityId(domain, "i") // activity id
					, "demotype" // activity type
					, new Instance(instance.getInstanceId(), instance.getLocation()) // Instance
					, null // time stamp
					, new InvokedBy(new UserId("test1@domain.com", domain)) // invoked by
					, null); // activity attributes

			lst.add(this.restTemplate.postForObject(this.url + "/activities/", activityBoundary,
					ActivityBoundary.class));
		}

//		WHEN I GET /iob/admin/activities with ADMIN user
//		THEN the server will respond with 15 activities
		assertThat(this.restTemplate.getForObject(
				this.url + "/admin/activities?userDomain={domain}&userEmail={email}&size={size}&page={page}",
				ActivityBoundary[].class, domain, user2.getEmail(), 15, 0)).hasSize(15)
						.usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrderElementsOf(lst);

//		WHEN I GET /iob/admin/activities with PLAYER user
//		THEN the server will respond with RuntimeException
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(
					this.url + "/admin/activities?userDomain={domain}&userEmail={email}&size={size}&page={page}",
					ActivityBoundary[].class, domain, user1.getEmail(), 15, 0);
		});

//		WHEN I GET /iob/admin/activities with invalid user email
//		THEN the server will respond with  RuntimeException
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(
					this.url + "/admin/activities?userDomain={domain}&userEmail={email}&size={size}&page={page}",
					ActivityBoundary[].class, domain, "invalid user email", 15, 0);
		});

//		WHEN I GET /iob/admin/activities with invalid domain
//		THEN the server will respond with  RuntimeException
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(
					this.url + "/admin/activities?userDomain={domain}&userEmail={email}&size={size}&page={page}",
					ActivityBoundary[].class, "invalid domain", user1.getEmail(), 15, 0);
		});

//		WHEN I GET /iob/admin/activities with invalid domain AND invalid user email
//		THEN the server will respond with  RuntimeException
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(
					this.url + "/admin/activities?userDomain={domain}&userEmail={email}&size={size}&page={page}",
					ActivityBoundary[].class, "invalid domain", "invalid user email", 15, 0);
		});

	}

	@DisplayName("Validate that only admins can delete all activities")
	@Test
	public void testDeleteAllActivities() {

//		GIVEN the server IP is up and there are valid PLAYER and ADMIN users in the DB,
//		AND there are valid INstance in the DB,
//		AND there are 15 activities in the DB

		NewUserBoundary user1 = new NewUserBoundary("test1@domain.com", "PLAYER", "test1", "808");
		NewUserBoundary user2 = new NewUserBoundary("test0@domain.com", "ADMIN", "test2", "8082");

		this.restTemplate.postForObject(this.url + "/users/", user1, UserBoundary.class);
		this.restTemplate.postForObject(this.url + "/users/", user2, UserBoundary.class);

		InstanceBoundary instanceBoundary = new InstanceBoundary(new InstanceId(domain, "1"), // instance id
				"drmotype", // instance type
				"testInstance", // instance name
				true, // is active
				null, // time stamp
				new CreatedBy(new UserId("test1@domain.com", domain)), // created by
				new Location(0.0, 0.0), // location
				new HashMap<String, Object>()); // instanceAttributes

		InstanceBoundary instance = this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary,
				InstanceBoundary.class);

		List<ActivityBoundary> lst = new ArrayList<>();
		for (int i = 0; i < 15; i++) {

			ActivityBoundary activityBoundary = new ActivityBoundary(new ActivityId(domain, "i") // activity id
					, "demotype" // activity type
					, new Instance(instance.getInstanceId(), instance.getLocation()) // Instance
					, null // time stamp
					, new InvokedBy(new UserId("test1@domain.com", domain)) // invoked by
					, null); // activity attributes

			lst.add(this.restTemplate.postForObject(this.url + "/activities/", activityBoundary,
					ActivityBoundary.class));
		}

//		WHEN I GET /iob/admin/activities with ADMIN user
//		THEN the server will respond with 200 OK
		assertThat(this.restTemplate.getForObject(this.url + "/admin/activities?userDomain={domain}&userEmail={email}",
				ActivityBoundary[].class, domain, user2.getEmail()));

//		WHEN I GET /iob/admin/activities with ADMIN user
//		THEN the server will respond with  RuntimeException
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(this.url + "/admin/activities?userDomain={domain}&userEmail={email}",
					ActivityBoundary[].class, domain, user1.getEmail());
		});

//		WHEN I GET /iob/admin/activities with invalid user email
//		THEN the server will respond with 15 RuntimeException
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(this.url + "/admin/activities?userDomain={domain}&userEmail={email}",
					ActivityBoundary[].class, domain, "invalid user email");
		});

//		WHEN I GET /iob/admin/activities with invalid domain
//		THEN the server will respond with 15 RuntimeException
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(this.url + "/admin/activities?userDomain={domain}&userEmail={email}",
					ActivityBoundary[].class, "invalid domain", user1.getEmail());
		});

//		WHEN I GET /iob/admin/activities with invalid domain AND invalid user email
//		THEN the server will respond with 15 RuntimeException
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(this.url + "/admin/activities?userDomain={domain}&userEmail={email}",
					ActivityBoundary[].class, "invalid domain", "invalid user email");
		});

	}

}
