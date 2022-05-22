package iob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Arrays;
import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import iob.restapi.boundaries.InstanceBoundary;
import iob.restapi.boundaries.NewUserBoundary;
import iob.restapi.boundaries.UserBoundary;
import iob.restapi.objects.CreatedBy;
import iob.restapi.objects.InstanceId;
import iob.restapi.objects.Location;
import iob.restapi.objects.UserId;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class InstanceTest {

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

	@Test
	@DisplayName("get search by name according role permissions")
	public void SearchByNameAccordingPermission() throws Exception {

		NewUserBoundary user1 = new NewUserBoundary("test0@domain.com", "PLAYER", "test1", "808");
		NewUserBoundary user2 = new NewUserBoundary("test1@domain.com", "ADMIN", "test2", "8082");
		NewUserBoundary user3 = new NewUserBoundary("test2@domain.com", "MANAGER", "test2", "8082");

		UserBoundary boundary1 = this.restTemplate.postForObject(this.url + "/users/", user1, UserBoundary.class);
		UserBoundary boundary2 = this.restTemplate.postForObject(this.url + "/users/", user2, UserBoundary.class);
		UserBoundary boundary3 = this.restTemplate.postForObject(this.url + "/users/", user3, UserBoundary.class);

		InstanceBoundary instanceBoundary = new InstanceBoundary(new InstanceId(domain, "1"), // instance id
				"drmotype", // instance type
				"testInstance", // instance name
				true, // is active
				null, // time stamp
				new CreatedBy(new UserId("test1@domain.com", domain)), // created by
				new Location(0.0, 0.0), // location
				new HashMap<String, Object>()); // instanceAttributes

		this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary, InstanceBoundary.class);

		// when role is player we get only in the same name and only active==true
		InstanceBoundary[] ins = this.restTemplate.getForObject(
				this.url + "/instances/search/ByName/{name}?userDomain={domain}&userEmail={email}",
				InstanceBoundary[].class, "testInstance", boundary1.getUserId().getDomain(),
				boundary1.getUserId().getEmail());

		System.err.println(Arrays.toString(ins));
		// when role is player and we have true active
		assertThat(ins).hasSize(2);
		assertThat(ins[0].getName().equals(instanceBoundary.getName()));

		// when role is admin we get runtimeExeption Permission denied
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(
					this.url + "/instances/search/ByName/{name}?userDomain={domain}&userEmail={email}",
					InstanceBoundary[].class, "testInstance", boundary2.getUserId().getDomain(),
					boundary2.getUserId().getEmail());
		});

		InstanceBoundary instanceBoundary2 = new InstanceBoundary(new InstanceId(domain, "1"), // instance id
				"drmotype", // instance type
				"testInstance", // instance name
				false, // is active
				null, // time stamp
				new CreatedBy(new UserId("test1@domain.com", domain)), // created by
				new Location(0.0, 0.0), // location
				new HashMap<String, Object>()); // instanceAttributes

		this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary2, InstanceBoundary.class);

		// when role is manager we get according name not matter active
		InstanceBoundary[] inst = this.restTemplate.getForObject(
				this.url + "/instances/search/ByName/{name}?userDomain={domain}&userEmail={email}",
				InstanceBoundary[].class, "testInstance", boundary3.getUserId().getDomain(),
				boundary3.getUserId().getEmail());

		assertThat(inst).hasSize(4);
	}

	@Test
	@DisplayName("get search by Type according role permissions")
	public void SearchByTypeAccordingPermission() throws Exception {

		NewUserBoundary user1 = new NewUserBoundary("test0@domain.com", "PLAYER", "test1", "808");
		NewUserBoundary user2 = new NewUserBoundary("test1@domain.com", "ADMIN", "test2", "8082");
		NewUserBoundary user3 = new NewUserBoundary("test2@domain.com", "MANAGER", "test2", "8082");

		UserBoundary boundary1 = this.restTemplate.postForObject(this.url + "/users/", user1, UserBoundary.class);
		UserBoundary boundary2 = this.restTemplate.postForObject(this.url + "/users/", user2, UserBoundary.class);
		UserBoundary boundary3 = this.restTemplate.postForObject(this.url + "/users/", user3, UserBoundary.class);

		InstanceBoundary instanceBoundary = new InstanceBoundary(new InstanceId(domain, "1"), // instance id
				"drmotype", // instance type
				"testInstance", // instance name
				true, // is active
				null, // time stamp
				new CreatedBy(new UserId("test1@domain.com", domain)), // created by
				new Location(0.0, 0.0), // location
				new HashMap<String, Object>()); // instanceAttributes

		this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary, InstanceBoundary.class);

		// when role is player we get only in the same type and only active==true
		InstanceBoundary[] instance = this.restTemplate.getForObject(
				this.url + "/instances/search/ByType/{type}?userDomain={domain}&userEmail={email}",
				InstanceBoundary[].class, "drmotype", boundary1.getUserId().getDomain(),
				boundary1.getUserId().getEmail());
		// when role is player and we have true active
		assertThat(instance).hasSize(1);
		assertThat(instance[0].getName().equals(instanceBoundary.getName()));

		// when role is admin we get runtimeExeption Permission denied
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(
					this.url + "/instances/search/ByType/{type}?userDomain={domain}&userEmail={email}",
					InstanceBoundary[].class, "drmotype", boundary2.getUserId().getDomain(),
					boundary2.getUserId().getEmail());
		});

		InstanceBoundary instanceBoundary2 = new InstanceBoundary(new InstanceId(domain, "1"), // instance id
				"drmotype", // instance type
				"testInstance", // instance name
				false, // is active
				null, // time stamp
				new CreatedBy(new UserId("test1@domain.com", domain)), // created by
				new Location(0.0, 0.0), // location
				new HashMap<String, Object>()); // instanceAttributes

		this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary2, InstanceBoundary.class);

		// when role is manager we get according type not matter active
		InstanceBoundary[] inst = this.restTemplate.getForObject(
				this.url + "/instances/search/ByType/{type}?userDomain={domain}&userEmail={email}",
				InstanceBoundary[].class, "drmotype", boundary3.getUserId().getDomain(),
				boundary3.getUserId().getEmail());

		assertThat(inst).hasSize(2);

	}
//	@Test
//	public void InstanceUpdateTest() {
//		
//		NewUserBoundary user1 = new NewUserBoundary("test0@domain.com", "PLAYER", "test1", "808");
//		NewUserBoundary user2 = new NewUserBoundary("test1@domain.com", "ADMIN", "test2", "8082");
//		NewUserBoundary user3 = new NewUserBoundary("test2@domain.com", "MANAGER", "test2", "8082");
//		
//		UserBoundary boundary1 = this.restTemplate.postForObject(this.url + "/users/", user1, UserBoundary.class);
//		UserBoundary boundary2 = this.restTemplate.postForObject(this.url + "/users/", user2, UserBoundary.class);
//		UserBoundary boundary3 = this.restTemplate.postForObject(this.url + "/users/", user3, UserBoundary.class);
//		
//		
//		InstanceBoundary instanceBoundary = new InstanceBoundary(new InstanceId("domain", "1"), // instance id
//				"drmotype", // instance type
//				"testInstance", // instance name
//				true, // is active
//				null, // time stamp
//				new CreatedBy(new UserId("test1@domain.com", domain)), // created by
//				new Location(0.0, 0.0), // location
//				new HashMap<String, Object>()); // instanceAttributes
//				
//		InstanceBoundary in =	this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary,
//				InstanceBoundary.class);
//				
//		
//		InstanceBoundary instanceBoundaryUpdate = new InstanceBoundary(new InstanceId("domain", "1"), // instance id
//				"drmotype", // instance type
//				"testInstance", // instance name
//				true, // is active
//				null, // time stamp
//				new CreatedBy(new UserId("test1@domain.com", "domain")), 
//				new Location(30.3, 20.0), // location
//				new HashMap<String, Object>()); // instanceAttributes
//		
//		
//		 this.restTemplate.put(this.url + "/instances/{instanceDomain}/{instanceId}?userDomain={domain}&userEmail={email}",
//		 			instanceBoundaryUpdate, 			
//		 			 instanceBoundary.getInstanceId().getDomain(),
//		 			 instanceBoundary.getInstanceId().getId(),
//		 			boundary3.getUserId().getDomain(),
//		 			boundary3.getUserId().getEmail());
//		 			
//				
//			InstanceBoundary[] inst = this.restTemplate.getForObject(this.url + "/instances/search/ByType/{type}?userDomain={domain}&userEmail={email}"
//					, InstanceBoundary[].class,"drmotype" 
//					, boundary3.getUserId().getDomain()
//					, boundary3.getUserId().getEmail());
//		 
//				
//		 	assertThat(inst).hasSize(1);
//		 	assertThat(inst[0].getLocation().getLat() == 30.3);
//	}

	@Test
	@DisplayName("get all instances according role permissions")
	public void getAllInstancesAcordingRole() {

		NewUserBoundary user1 = new NewUserBoundary("test0@domain.com", "PLAYER", "test1", "808");
		NewUserBoundary user2 = new NewUserBoundary("test1@domain.com", "ADMIN", "test2", "8082");
		NewUserBoundary user3 = new NewUserBoundary("test2@domain.com", "MANAGER", "test2", "8082");

		UserBoundary boundary1 = this.restTemplate.postForObject(this.url + "/users/", user1, UserBoundary.class);
		UserBoundary boundary2 = this.restTemplate.postForObject(this.url + "/users/", user2, UserBoundary.class);
		UserBoundary boundary3 = this.restTemplate.postForObject(this.url + "/users/", user3, UserBoundary.class);

		InstanceBoundary instanceBoundary = new InstanceBoundary(new InstanceId("domain", "1"), // instance id
				"drmotype", // instance type
				"testInstance", // instance name
				false, // is active
				null, // time stamp
				new CreatedBy(new UserId("test1@domain.com", domain)), // created by
				new Location(0.0, 0.0), // location
				new HashMap<String, Object>()); // instanceAttributes

		this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary, InstanceBoundary.class);

		this.restTemplate.postForObject(this.url + "/instances/", instanceBoundary, InstanceBoundary.class);

		// when role is manager we get according type not matter active
		InstanceBoundary[] inst = this.restTemplate.getForObject(
				this.url + "/instances?userDomain={domain}&userEmail={email}", InstanceBoundary[].class,
				boundary3.getUserId().getDomain(), boundary3.getUserId().getEmail());

		assertThat(inst).hasSize(6);

		// when role is admin we get not permission
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(this.url + "/instances?userDomain={domain}&userEmail={email}",
					InstanceBoundary.class, boundary2.getUserId().getDomain(), boundary2.getUserId().getEmail());
		});

		// when role is player we get according active == true
		InstanceBoundary[] inst2 = this.restTemplate.getForObject(
				this.url + "/instances?userDomain={domain}&userEmail={email}", InstanceBoundary[].class,
				boundary1.getUserId().getDomain(), boundary1.getUserId().getEmail());

		assertThat(inst2).hasSize(2);
	}

}
