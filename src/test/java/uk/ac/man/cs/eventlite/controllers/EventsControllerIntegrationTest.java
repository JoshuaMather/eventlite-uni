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

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;
    
	private WebTestClient client;
    
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
	public void deleteEventNoUser() {
		
		client.delete().uri("/events/8").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
				.expectHeader().value("Location", endsWith("/sign-in"));

		assertThat(rows, equalTo(countRowsInTable("event")));
	}

	@Test
	@DirtiesContext
	public void deleteEventWithUser() {
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
