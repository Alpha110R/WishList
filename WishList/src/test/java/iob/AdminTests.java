package iob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import iob.restapi.boundaries.NewUserBoundary;
import iob.restapi.boundaries.UserBoundary;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AdminTests {

	private int port;
	private String url;
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

	@DisplayName("Validate that only admins can get users and delete all")
	@Test
	public void testAdminCommands() {
		// when i post 3 users and one of them is an ADMIN
		// the server return 200 OK for all of them

		NewUserBoundary user1 = new NewUserBoundary("goo@boo.foo", "PLAYER", "bob1", "808");
		NewUserBoundary user2 = new NewUserBoundary("boo@goo.oof", "PLAYER", "bob2", "8082");
		NewUserBoundary admin1 = new NewUserBoundary("loo@loo.koo", "ADMIN", "master", "666");

		this.restTemplate.postForObject(this.url + "/users/", user1, UserBoundary.class);
		this.restTemplate.postForObject(this.url + "/users/", user2, UserBoundary.class);
		this.restTemplate.postForObject(this.url + "/users/", admin1, UserBoundary.class).toString();

		// when i try to access getUser with not-admin it fails
		assertThatExceptionOfType(HttpServerErrorException.class).isThrownBy(() -> {
			this.restTemplate.getForObject(
					this.url + "/admin/users?userEmail=goo@boo.foo&userDomain=2022b.timor.bystritskie",
					UserBoundary[].class);
		});

		// when i try to access getUsers with an admin it succeeds
		assertThat(this.restTemplate.getForObject(
				this.url + "/admin/users?userEmail=loo@loo.koo&userDomain=2022b.timor.bystritskie",
				UserBoundary[].class)).hasSize(3);

		// when i try to access delete with not-admin it fails
		// when i try to access delete with an admin it succeeds

	}

}
