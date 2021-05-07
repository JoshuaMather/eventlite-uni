package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import uk.ac.man.cs.eventlite.testutil.FormUtil;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;
    
	private WebTestClient client;
    
	@Autowired
	private EventService eventService;
	
	private int rows;
	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String SESSION_KEY = "JSESSIONID";
	private static String CSRF_HEADER = "X-CSRF-TOKEN";
	
	@BeforeEach
	public void setup() {
        rows = countRowsInTable("event");
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}
	
	@Test
	public void testGetSingleEvent() {
		client.get().uri("/events/9").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML)
		.expectBody(String.class)
		.consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("COMP23412 Showcase, group F"));
		});
	}
	
	@Test
	public void testGetNewEvent() {
		String[] tokens = login();
		
		client.get().uri("/events/new").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1])
		.exchange().expectStatus().isOk();

	}
	
	@Test
	public void testCreateEventNoUser() {
		String[] tokens = login();
		
		LocalDate date = LocalDate.of(2099, 3, 10);
		Venue venue = new Venue();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		String localtime = LocalTime.now().format(dtf);
		
		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", tokens[0]);
		event.add("name", "event");
		event.add("date", date.toString());
		event.add("v_id", String.valueOf(venue.getId()));
		event.add("description", "description");
		event.add("time", localtime);
		
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(event).exchange().expectStatus().isFound()
		.expectHeader().value("Location", endsWith("/sign-in"));

		assertThat(rows, equalTo(countRowsInTable("event")));
	}
	
	@Test
	public void testCreateEvent() {
		String[] tokens = login();
		
		LocalDate date = LocalDate.of(2099, 3, 10);
		Venue venue = new Venue();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		String localtime = LocalTime.now().format(dtf);
		
		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", tokens[0]);
		event.add("name", "event");
		event.add("date", date.toString());
		event.add("v_id", String.valueOf(venue.getId()));
		event.add("description", "description");
		event.add("time", localtime);
		
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(event).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound()
		.expectHeader().value("Location", endsWith("/events"));
	
		assertThat(rows+1, equalTo(countRowsInTable("event")));
		
		//See if the saved event values are correct
		Iterable<Event> events = eventService.findByName("event");
		Event new_event = events.iterator().next();
		assertEquals("event", new_event.getName());
		assertEquals(date.toString(), new_event.getDate().toString());
		assertEquals(String.valueOf(venue.getId()), String.valueOf(new_event.getV_id()));
		assertEquals("description", new_event.getDescription());
		assertEquals(localtime, new_event.getTime().toString());
	}
	
	@Test
	public void testCreateEventNoData() {
		String[] tokens = login();
		
		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", tokens[0]);
		event.add("name", "");
		event.add("date", "");
		event.add("v_id", "");
		event.add("description", "");
		event.add("time", "");
		
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(event).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isOk();
	
		// check no new event has been added
		assertThat(rows, equalTo(countRowsInTable("event")));
	}
	
	@Test
	public void testCreateEventBadData() {
		String[] tokens = login();
		
		LocalDate date = LocalDate.of(1900, 3, 10);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		String localtime = LocalTime.now().format(dtf);
		
		MultiValueMap<String, String> event = new LinkedMultiValueMap<>();
		event.add("_csrf", tokens[0]);
		event.add("name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		event.add("date", date.toString());
		event.add("v_id", "1");
		event.add("description", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		event.add("time", localtime);
		
		client.post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(event).header(CSRF_HEADER, tokens[0]).cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isOk();
	
		// check no new event has been added
		assertThat(rows, equalTo(countRowsInTable("event")));
	}
	
	@Test
    public void testSearchEvent() {
        client.get().uri("/events/search/?search=COMP23412+lab").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.TEXT_HTML)
        .expectBody(String.class)
        .consumeWith(result -> {
            assertThat(result.getResponseBody(), containsString("COMP23412 lab"));
        });
    }
	
	@Test
    public void testSearchEventNoResult() {
        client.get().uri("/events/search/?search=nosuchevent").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.TEXT_HTML)
        .expectBody(String.class)
        .consumeWith(result -> {
            assertThat(result.getResponseBody(), containsString("There are no results found for your search."));
        });
    }
	
	@Test
	public void testUpdateEventNoUser() {
		String[] tokens = login();

		// Attempt to POST a valid data
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("e_id", "8");
		form.add("name", "name");
		form.add("date", "2021-12-31");
		form.add("time", "15:00");
		form.add("v_id", "5");
		form.add("description", "test description");

		// session ID not set, so no credentials.
		// This should redirect to the sign-in page.
		client.post().uri("/events/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/sign-in"));

		// nothing should be added
		assertThat(rows, equalTo(countRowsInTable("event")));
	}
	
	@Test
	@DirtiesContext
	public void testUpdateEventWithUser() {
		String[] tokens = login();

		// Attempt to POST a valid data
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", tokens[0]);
		form.add("e_id", "8");
		form.add("name", "name");
		form.add("date", "2021-12-31");
		form.add("time", "15:00");
		form.add("v_id", "5");
		form.add("description", "test description");
		
		client.post().uri("/events/8/update").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus()
				.isFound()
				.expectHeader()
				.value("Location", endsWith("/events/8"));

		
		assertThat(rows, equalTo(countRowsInTable("event")));
	}
	
	@Test
	public void testDeleteEventNoUser() {
		
		client.delete().uri("/events/8").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
				.expectHeader().value("Location", endsWith("/sign-in"));

		assertThat(rows, equalTo(countRowsInTable("event")));
	}

	@Test
	@DirtiesContext
	public void testDeleteEventWithUser() {
		String[] tokens = login();

		client.delete().uri("/events/8").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/events"));

		assertThat(rows - 1, equalTo(countRowsInTable("event")));
	}
	
	private String[] login() {
		String[] tokens = new String[2];

		EntityExchangeResult<String> result = client.mutate()
				.filter(basicAuthentication("Mustafa", "Mustafa")).build().get()
				.uri("/").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = csrfToken(result.getResponseBody());
		
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}
	
	private String csrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);
		
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}
	
}
