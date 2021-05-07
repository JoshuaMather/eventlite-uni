package uk.ac.man.cs.eventlite.controllers;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;
	
	private int rows;

	private WebTestClient client;
	
	@Autowired
	private VenueService venueService;
	
	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String SESSION_KEY = "JSESSIONID";
	private static String CSRF_HEADER = "X-CSRF-TOKEN";

	@BeforeEach
	public void setup() {
		rows = countRowsInTable("venue");
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}
	
	@Test
	public void testGetSingleVenue() {
		client.get().uri("/venues/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML)
		.expectBody(String.class)
		.consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Kilburn G23"));
		});
	}
	
	@Test
	public void testGetNewVenue() {
		String[] tokens = login();
		
		client.get().uri("/venues/new").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1])
		.exchange().expectStatus().isOk();

	}
	
	@Test
	public void testCreateVenueNoUser() {
		String[] tokens = login();
		
		MultiValueMap<String, String> venue = new LinkedMultiValueMap<>();
		venue.add("_csrf", tokens[0]);
		venue.add("name", "venue");
		venue.add("road", "4 Oxford Road");
		venue.add("postcode", "M13 9PL");
		venue.add("capacity", "20");
		
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(venue).exchange().expectStatus().isFound()
		.expectHeader().value("Location", endsWith("/sign-in"));

		assertThat(rows, equalTo(countRowsInTable("venue")));
	}
	
	@Test
	public void testCreateVenue() {
		String[] tokens = login();
		
		MultiValueMap<String, String> venue = new LinkedMultiValueMap<>();
		venue.add("_csrf", tokens[0]);
		venue.add("name", "venue");
		venue.add("road", "4 Oxford Road");
		venue.add("postcode", "M13 9PL");
		venue.add("capacity", "20");
		
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(venue).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound()
		.expectHeader().value("Location", endsWith("/venues"));
	
		assertThat(rows+1, equalTo(countRowsInTable("venue")));
		
		//See if the saved venue values are correct
		Iterable<Venue> venues = venueService.findByName("venue");
		Venue new_venue = venues.iterator().next();
		assertEquals("venue", new_venue.getName());
		assertEquals("4 Oxford Road", new_venue.getRoad());
		assertEquals("M13 9PL", new_venue.getPostcode());
		assertEquals("20", String.valueOf(new_venue.getCapacity()));
		assertEquals(new_venue.getLatitude(), 53.476429);
		assertEquals(new_venue.getLongitude(), -2.23217);
		
	}
	
	@Test
	public void testCreateVenueNoData() {
		String[] tokens = login();
		
		MultiValueMap<String, String> venue = new LinkedMultiValueMap<>();
		venue.add("_csrf", tokens[0]);
		venue.add("name", "");
		venue.add("road", "");
		venue.add("postcode", "");
		venue.add("capacity", "");
		
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(venue).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isOk();
	
		// check no new venue has been added
		assertThat(rows, equalTo(countRowsInTable("venue")));
	}
	
	@Test
	public void testCreateVenueBadData() {
		String[] tokens = login();
		
		MultiValueMap<String, String> venue = new LinkedMultiValueMap<>();
		venue.add("_csrf", tokens[0]);
		venue.add("name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		venue.add("road", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		venue.add("postcode", "AAA");
		venue.add("capacity", "-1");
		
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(venue).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isOk();
	
		// check no new venue has been added
		assertThat(rows, equalTo(countRowsInTable("venue")));
	}
	
	@Test
    public void testSearchVenue() {
        client.get().uri("/venues/search/?search=Kilburn+LF31").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.TEXT_HTML)
        .expectBody(String.class)
        .consumeWith(result -> {
            assertThat(result.getResponseBody(), containsString("Kilburn LF31"));
        });
    }
	
	@Test
    public void testSearchVenueNoResult() {
        client.get().uri("/venues/search/?search=nosuchvenue").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.TEXT_HTML)
        .expectBody(String.class)
        .consumeWith(result -> {
            assertThat(result.getResponseBody(), containsString("There are no results found for your search."));
        });
    }
	
	@Test
	public void testGetUpdateVenue() {
		String[] tokens = login();
		
		client.get().uri("/venues/1/update").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1])
		.exchange().expectStatus().isOk();

	}
	
	@Test
	public void testUpdateVenueNoUser() {
		String[] tokens = login();

		// Attempt to POST a valid data
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("v_id", "1");
		form.add("name", "venue");
		form.add("road", "road");
		form.add("postcode", "postcode");
		form.add("capacity", "1");

		// session ID not set, so no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/venue/1/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));

		// nothing should be added
		assertThat(rows, equalTo(countRowsInTable("venue")));
	}
	
	@Test
	@DirtiesContext
	public void testUpdateVenueWithUser() {
		String[] tokens = login();

		// Attempt to POST a valid data
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("v_id", "1");
		form.add("name", "venue");
		form.add("road", "road");
		form.add("postcode", "postcode");
		form.add("capacity", "1");
		
		client.post().uri("/venues/1/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus()
				.isFound()
				.expectHeader()
				.value("Location", endsWith("/venues/1"));

		
		assertThat(rows, equalTo(countRowsInTable("venue")));
		
		// check values are updated
		Venue new_venue = venueService.findById(1);
		assertEquals("venue", new_venue.getName());
		assertEquals("road", new_venue.getRoad());
		assertEquals("postcode", new_venue.getPostcode());
		assertEquals("1", String.valueOf(new_venue.getCapacity()));
	}
	
	@Test
	@DirtiesContext
	public void testUpdateVenueNoData() {
		String[] tokens = login();

		// Attempt to POST a valid data
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("v_id", "1");
		form.add("name", "");
		form.add("date", "");
		form.add("time", "");
		form.add("v_id", "");
		form.add("description", "");

		client.post().uri("/venues/1/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound()
		.expectHeader().value("Location", endsWith("/venues/1/update"));
		
		assertThat(rows, equalTo(countRowsInTable("venue")));
		
		// check values are the same 
		Venue new_venue = venueService.findById(1);
		assertEquals("Kilburn G23", new_venue.getName());
		assertEquals("M13 9PL", new_venue.getRoad());
		assertEquals("M13 9PL", new_venue.getPostcode());
		assertEquals("80", String.valueOf(new_venue.getCapacity()));
	}
	
	@Test
	@DirtiesContext
	public void testUpdateVenueBadData() {
		String[] tokens = login();

		// Attempt to POST a valid data
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("v_id", "1");
		form.add("name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		form.add("road", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		form.add("postcode", "AAA");
		form.add("capacity", "-1");
		
		client.post().uri("/venues/1/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound()
		.expectHeader().value("Location", endsWith("/venues/1/update"));
		
		assertThat(rows, equalTo(countRowsInTable("venue")));
		
		// check values are the same 
		Venue new_venue = venueService.findById(1);
		assertEquals("Kilburn G23", new_venue.getName());
		assertEquals("M13 9PL", new_venue.getRoad());
		assertEquals("M13 9PL", new_venue.getPostcode());
		assertEquals("80", String.valueOf(new_venue.getCapacity()));
	}
	
	@Test
	public void deleteVenueNoUser() {
		
		client.delete().uri("/venues/6").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
				.expectHeader().value("Location", endsWith("/sign-in"));

		assertThat(rows, equalTo(countRowsInTable("venue")));
	}
	

	@Test
	@DirtiesContext
	public void deleteVenueWithUser() {
		String[] tokens = login();

		client.delete().uri("/venues/4").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/venues"));
        
		assertThat(rows - 1, equalTo(countRowsInTable("venue")));
	}
	
	
	private String[] login() {
		String[] tokens = new String[2];

		EntityExchangeResult<String> result = client.mutate()
				.filter(basicAuthentication("Mustafa", "Mustafa")).build().get()
				.uri("/").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}
	
	private String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);

		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}
}