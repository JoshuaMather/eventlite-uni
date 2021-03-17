package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsControllerApi.class)
@Import(Security.class)
public class EventsControllerApiTest {

	private final static String BAD_ROLE = "USER";

	@Autowired
	private MockMvc mvc;
	
	private Event event;

	@MockBean
	private EventService eventService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		Venue v1 = new Venue();
		v1.setName("Venue");
		v1.setCapacity(80);
		
		Event e = new Event();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(v1);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)));

		verify(eventService).findAll();
	}
	
	@Test
	public void deleteAnEvent() throws Exception {
		Event event = new Event();
		Venue venue = new Venue();
		
		event.setId(0);
		event.setName("Event");
		event.setDate(LocalDate.now());
		event.setTime(LocalTime.now());
		event.setVenue(venue);
		when(eventService.findById(0)).thenReturn(event);
		
		mvc.perform(delete("/api/events/0").with(user("Mustafa").roles(Security.ADMIN_ROLE)).contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
		
		verify(eventService).deleteById(0);
	}
	
	@Test
	public void getNewEvent() throws Exception {
		mvc.perform(get("/api/events/new").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotAcceptable())
				.andExpect(handler().methodName("newEvent"));
	}
	
	@Test
	public void postEventNoAuth() throws Exception {
		mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"test\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEventBadAuth() throws Exception {
		mvc.perform(post("/api/events").with(anonymous()).contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"test\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEventBadRole() throws Exception {
		mvc.perform(post("/api/events").with(user("Rob").roles(BAD_ROLE)).contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"test\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEmptyNameEvent() throws Exception {
		mvc.perform(post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"name\": \"\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("")).andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEmptyDateEvent() throws Exception {
		mvc.perform(post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"date\": \"\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("")).andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEmptyVenueEvent() throws Exception {
		mvc.perform(post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"v_id\": \"\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("")).andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postLongNameEvent() throws Exception {
		mvc.perform(post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
				.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postPastDateEvent() throws Exception {
		LocalDate date = LocalDate.of(1900, 3, 10);
		
		mvc.perform(post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"date\": \"" +date.toString()+"\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("")).andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postTodayDateEvent() throws Exception {
		LocalDate date = LocalDate.now();
		
		mvc.perform(post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"date\": \"" +date.toString()+"\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("")).andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postLongDescriptionEvent() throws Exception {
		mvc.perform(post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"description\": \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
				.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEvent() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);

		LocalDate date = LocalDate.of(2099, 3, 10);
		Venue venue = new Venue();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		String localtime = LocalTime.now().format(dtf);
		
		mvc.perform(post("/api/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"test\",  \"date\": \""+date.toString()+"\", \"v_id\": \""+String.valueOf(venue.getId())+"\", \"description\": \"description\", \"time\": \""+localtime+"\"}")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andExpect(content().string(""))
				.andExpect(header().string("Location", containsString("/api/events/")))
				.andExpect(handler().methodName("createEvent"));

		verify(eventService).save(arg.capture());
		assertThat("test", equalTo(arg.getValue().getName()));
		assertThat(date.toString(), equalTo(arg.getValue().getDate().toString()));
		assertThat(String.valueOf(venue.getId()), equalTo(String.valueOf(arg.getValue().getId())));
		assertThat("description", equalTo(arg.getValue().getDescription()));
		assertThat(localtime, equalTo(arg.getValue().getTime().toString()));
	}
	
}
