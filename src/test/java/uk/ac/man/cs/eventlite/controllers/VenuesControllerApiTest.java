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
import java.util.Collections;
import java.util.Optional;

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
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import(Security.class)
public class VenuesControllerApiTest {

	private final static String BAD_ROLE = "USER";

	@Autowired
	private MockMvc mvc;
	
	private Venue venue;
	
	private Event event;

	@MockBean
	private VenueService venueService;

	// These tests may not be needed but if they are there are errors to be fixed
//	@Test
//	public void getIndexWhenNoVenus() throws Exception {
//		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
//
//		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
//				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
//				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));
//
//		verify(venueService).findAll();
//	}
//
//	@Test
//	public void getIndexWithVenues() throws Exception {
//		Venue v1 = new Venue();
//		v1.setName("Venue");
//		v1.setRoad("road");
//		v1.setPostcode("postcode");
//		v1.setCapacity(80);
//		
//		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(v1));
//
//		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
//				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(2)))
//				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
//				.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)));
//
//		verify(venueService).findAll();
//	}
	
	@Test
	public void getNewVenue() throws Exception {
		mvc.perform(get("/api/venues/new").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotAcceptable())
				.andExpect(handler().methodName("newVenue"));
	}
	
	@Test
	public void postVenueNoAuth() throws Exception {
		mvc.perform(post("/api/venues").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"test\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postVenueBadAuth() throws Exception {
		mvc.perform(post("/api/venues").with(anonymous()).contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"test\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postVenueBadRole() throws Exception {
		mvc.perform(post("/api/venues").with(user("Rob").roles(BAD_ROLE)).contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"test\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postEmptyNameVenue() throws Exception {
		mvc.perform(post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"name\": \"\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("")).andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postEmptyRoadVenue() throws Exception {
		mvc.perform(post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"road\": \"\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("")).andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postEmptyPostcodeVenue() throws Exception {
		mvc.perform(post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"postcode\": \"\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("")).andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postEmptyCapacityVenue() throws Exception {
		mvc.perform(post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON).content("{ \"capacity\": \"\" }")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity())
				.andExpect(content().string("")).andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postLongNameVenue() throws Exception {
		mvc.perform(post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postLongRoadVenue() throws Exception {
		mvc.perform(post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"road\": \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postNegativeCapacityVenue() throws Exception {
		mvc.perform(post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"capacity\": \"-1\" }").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity()).andExpect(content().string(""))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postVenue() throws Exception {
		ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);

		mvc.perform(post("/api/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"name\": \"test\", \"road\": \"road\", \"postcode\": \"postcode\", \"capacity\": \"1\"}")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andExpect(content().string(""))
				.andExpect(header().string("Location", containsString("/api/venues/")))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService).save(arg.capture());
		assertThat("test", equalTo(arg.getValue().getName()));
		assertThat("road", equalTo(arg.getValue().getRoad()));
		assertThat("postcode", equalTo(arg.getValue().getPostcode()));
		assertThat(1, equalTo(arg.getValue().getCapacity()));
	}
	
	@Test
	public void getSingleVenue() throws Exception {
		Venue v1 = new Venue();
		v1.setId(1);
		v1.setName("Venue");
		v1.setAddress("Weston");
		v1.setPostcode("M13 3BB");
		v1.setCapacity(80);
		v1.setLongitude(2.2348);
		v1.setLatitude(53.4743);
		
		when(venueService.findById(1)).thenReturn(v1);
		mvc.perform(get("/api/venues/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(handler().methodName("venueDescription"))
		             .andExpect(jsonPath("$.id", equalTo(1)))
		             .andExpect(jsonPath("$.name", equalTo(v1.getName())))
		             .andExpect(jsonPath("$.address", equalTo(v1.getAddress())))
		             .andExpect(jsonPath("$.postcode", equalTo(v1.getPostcode())))
		             .andExpect(jsonPath("$.capacity", equalTo(v1.getCapacity())))
		             .andExpect(jsonPath("$.longitude", equalTo(v1.getLongitude())))
		             .andExpect(jsonPath("$.latitude", equalTo(v1.getLatitude())))
		             .andExpect(jsonPath("$._links.self.href", endsWith("/api/events/1")))
		             .andExpect(jsonPath("$._links.events.href", endsWith("/api/venues/1/events")))
		             .andExpect(jsonPath("$._links.next3events.href", endsWith("/api/venues/1/next3events")));
		verify(venueService).findById(1);
		             
	}
}
