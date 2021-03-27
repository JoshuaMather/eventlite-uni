package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {
	
	private final static String BAD_ROLE = "USER";

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verify(venueService).findAll();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		Venue venue = new Venue();
		when(event.getVenue()).thenReturn(venue);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verify(venueService).findAll();
	}
	
	@Test
	public void deleteAnEvent() throws Exception {
		when(eventService.findById(0)).thenReturn(event);
		mvc.perform(delete("/events/0").with(user("Mustafa").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isFound()).andExpect(view().name("redirect:/events"));
			
	}
	
	@Test
	public void deleteEventNoAuth() throws Exception {
		when(eventService.findById(0)).thenReturn(event);
		mvc.perform(delete("/events/0").accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isFound())
		        .andExpect(header().string("Location", endsWith("/sign-in")));
	}
		
	
	@Test
	public void getNewEventNoAuth() throws Exception {
		mvc.perform(get("/events/new").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
	}

	@Test
	public void getNewEvent() throws Exception {
		mvc.perform(get("/events/new").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk()).andExpect(view().name("events/new"))
				.andExpect(handler().methodName("newEvent"));
	}
	
	@Test
	public void postEventNoAuth() throws Exception {
		mvc.perform(post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "test")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEventBadRole() throws Exception {
		mvc.perform(
				post("/events").with(user("Rob").roles(BAD_ROLE)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("name", "test").accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEmptyNameEvent() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "").accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "name"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEmptyDateEvent() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("date", "").accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "date"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEmptyVenueEvent() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("v_id", "").accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "v_id"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postLongNameEvent() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "name"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postPastDateEvent() throws Exception {
		LocalDate date = LocalDate.of(1900, 3, 10);
		
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("date", date.toString()).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "date"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postTodayDateEvent() throws Exception {
		LocalDate date = LocalDate.now();
		
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("date", date.toString()).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "date"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postLongDescriptionEvent() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("description", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "description"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEvent() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);

		
		LocalDate date = LocalDate.of(2099, 3, 10);
		Venue venue = new Venue();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		String localtime = LocalTime.now().format(dtf);
		
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "test").param("date", date.toString())
				.param("v_id", String.valueOf(venue.getId()))
				.param("description", "description").param("time", localtime)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(content().string(""))
				.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeExists("ok_message"));

		verify(eventService).save(arg.capture());
		assertThat("test", equalTo(arg.getValue().getName()));
		assertThat(date.toString(), equalTo(arg.getValue().getDate().toString()));
		assertThat(String.valueOf(venue.getId()), equalTo(String.valueOf(arg.getValue().getV_id())));
		assertThat("description", equalTo(arg.getValue().getDescription()));
		assertThat(localtime, equalTo(arg.getValue().getTime().toString()));
	}

}
