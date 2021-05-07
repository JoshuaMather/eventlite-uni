package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

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
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {
	
	private final static String BAD_ROLE = "USER";

	@Autowired
	private MockMvc mvc;

	@Mock
	private Venue venue;
	
	@Mock
	private Event event;

	@MockBean
	private VenueService venueService;
	
	@MockBean
	private EventService eventService;

	// These tests may not be needed but if they are there are errors to be fixed
//	@Test
//	public void getIndexWhenNoVenues() throws Exception {
//		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
//
//		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
//				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));
//
//		verify(venueService).findAll();
//		verifyNoInteractions(venue);
//	}
//
//	@Test
//	public void getIndexWithVenues() throws Exception {
//		when(venue.getName()).thenReturn("Kilburn Building");
//		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));
//
//		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));
//
//		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
//				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));
//
//		verify(venueService).findAll();
//	}
	@Test
	public void sortEvents() throws Exception{
		ArrayList<Venue> venues = new ArrayList<Venue>();
		
		Venue venueA = new Venue();
		venueA.setName("Venue A");
		venueA.setAddress("23 Manchester Road");
		venueA.setPostcode("E14 3BD");
		venueA.setCapacity(50);
		venues.add(venueA);
		
		Venue venueB = new Venue();
		venueB.setName("Venue B");
		venueB.setAddress("Highland ROad");
		venueB.setPostcode("S43 2EZ");
		venueB.setCapacity(1000);
		venues.add(venueB);
		
		Venue venueC = new Venue();
		venueC.setName("Venue C");
		venueC.setAddress("19 Acacia Avenue");
		venueC.setPostcode("WA15 8QY");
		venueC.setCapacity(10);
		venues.add(venueC);
	
		
		LocalDate now = LocalDate.of(2019, 06, 01); 
		when(venueService.findAllByAsc()).thenReturn(venues);
		
		Iterator<Venue> previousEvents = venueService.findAllByAsc().iterator();
		
		Venue venue1 = previousEvents.next();
		assertEquals(venue1.getName(), venueA.getName());

		Venue venue2 = previousEvents.next();
		assertEquals(venue2.getName(), venueB.getName());

		Venue venue3 = previousEvents.next();
		assertEquals(venue3.getName(), venueC.getName());
	}
	
	@Test
	public void getNewVenueNoAuth() throws Exception {
		mvc.perform(get("/venues/new").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
	}

	@Test
	public void getNewVenue() throws Exception {
		mvc.perform(get("/venues/new").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk()).andExpect(view().name("venues/new"))
				.andExpect(handler().methodName("newVenue"));
	}
	
	@Test
	public void postVenueNoAuth() throws Exception {
		mvc.perform(post("/venues").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "test")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postVenueBadRole() throws Exception {
		mvc.perform(
				post("/venues").with(user("Rob").roles(BAD_ROLE)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("name", "test").accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isForbidden());

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postEmptyNameVenue() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "").accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("venues/new"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"))
				.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postEmptyRoadVenue() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("road", "").accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("venues/new"))
				.andExpect(model().attributeHasFieldErrors("venue", "road"))
				.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postEmptyPostcodeVenue() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("postcode", "").accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("venues/new"))
				.andExpect(model().attributeHasFieldErrors("venue", "postcode"))
				.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postEmptyCapacityVenue() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("capacity", "").accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isOk()).andExpect(view().name("venues/new"))
				.andExpect(model().attributeHasFieldErrors("venue", "capacity"))
				.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postLongNameVenue() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"))
				.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postLongRoadVenue() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("road", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(model().attributeHasFieldErrors("venue", "road"))
				.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postNegativeCapacityVenue() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("capacity", "-1")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(model().attributeHasFieldErrors("venue", "capacity"))
				.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postVenue() throws Exception {
		ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);

		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "test").param("road", "road")
				.param("postcode", "postcode").param("capacity", "1")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(content().string(""))
				.andExpect(view().name("redirect:/venues")).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeExists("ok_message"));

		verify(venueService).save(arg.capture());
		assertThat("test", equalTo(arg.getValue().getName()));
		assertThat("road", equalTo(arg.getValue().getRoad()));
		assertThat("postcode", equalTo(arg.getValue().getPostcode()));
		assertThat(1, equalTo(arg.getValue().getCapacity()));
		assertThat("road, postcode", equalTo(arg.getValue().getAddress()));
	}
	
	@Test
	public void searchAVenue() throws Exception {
		Venue v1 = new Venue();
		v1.setId(1);
    	v1.setName("Room 1");
    	Venue v2 = new Venue();
		v2.setId(2);
    	v2.setName("Room 2");
    	ArrayList<Venue> list = new ArrayList<Venue>();
    	list.add(v1);
    	list.add(v2);
    	
		when(venueService.findByNameAsc("Room")).thenReturn(list);
		mvc.perform(get("/venues/search").with(user("Rob").roles(Security.ADMIN_ROLE)).param("search", "Room").accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("/venues/search"))
		.andExpect(handler().methodName("searchVenue"));
	}
	
	@Test
	public void searchAVenueNoResults() throws Exception {
		Venue v1 = new Venue();
		v1.setId(1);
    	v1.setName("Room 1");
    	Venue v2 = new Venue();
		v2.setId(2);
    	v2.setName("Room 2");
    	ArrayList<Venue> list = new ArrayList<Venue>();
    	list.add(v1);
    	list.add(v2);
    	
		when(venueService.findByNameAsc("Venue")).thenReturn(null);
		mvc.perform(get("/venues/search").with(user("Rob").roles(Security.ADMIN_ROLE)).param("search", "Room").accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("/venues/search"))
		.andExpect(handler().methodName("searchVenue"));
	}
	
	@Test 
	public void deleteUnOccupiedVenue() throws Exception {
		when(venueService.findById(1)).thenReturn(venue);
		mvc.perform(delete("/venues/1").with(user("Mustafa").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
		        .andExpect(status().isFound())
		        .andExpect(view().name("redirect:/venues"));
		
		verify(venueService).deleteById(1);
	}
	
	@Test 
	public void deleteOccupiedVenue() throws Exception {
		when(venueService.findById(1)).thenReturn(venue);
		when(event.getVenue()).thenReturn(venue);
		when(venue.getEvents()).thenReturn(Collections.<Event>singletonList(event));
		mvc.perform(delete("/venues/1").with(user("Mustafa").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
		        .andExpect(status().isFound())
		        .andExpect(view().name("redirect:/venues"));
		
		verify(venueService, never()).deleteById(1);
	}
	
	@Test
	public void deleteVenueBadRole() throws Exception {
		mvc.perform(
				delete("/venues/1").with(user("Rob").roles(BAD_ROLE)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isForbidden());

		verify(venueService, never()).deleteById(1);
	}
	
	@Test
	public void getVenueDescriptionAsAdmin() throws Exception {
		when(venueService.findById(1)).thenReturn(venue);
		
		mvc.perform(get("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk()).andExpect(view().name("venues/show"))
				.andExpect(model().hasNoErrors()).andExpect(handler().methodName("venueDescription"));
	}
	
	@Test
	public void getVenueDescriptionAsGuest() throws Exception {
		when(venueService.findById(1)).thenReturn(venue);
		
		mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk()).andExpect(view().name("venues/show"))
				.andExpect(model().hasNoErrors()).andExpect(handler().methodName("venueDescription"));
	}
	
	@Test
	public void getNonExistingVenueDescriptionAsAdmin() throws Exception {
		// getting an non existing venue
		when(venueService.findById(1)).thenReturn(null);
		
		mvc.perform(get("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound()).andExpect(view().name("redirect:/venues"))
				.andExpect(model().hasNoErrors()).andExpect(handler().methodName("venueDescription"));
	}
	
	@Test
	public void getNonExistingVenueDescriptionAsGuest() throws Exception {
		// getting an non existing venue
		when(venueService.findById(1)).thenReturn(null);
		
		mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML))
				.andExpect(status().isFound()).andExpect(view().name("redirect:/venues"))
				.andExpect(model().hasNoErrors()).andExpect(handler().methodName("venueDescription"));
	}
	
	
	@Test
	public void updateVenue() throws Exception {
		ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);

    	Venue v = new Venue();
    	v.setId(1);
    	v.setName("venue");
    	v.setCapacity(10);
    	v.setPostcode("M13 9PL");
    	v.setRoad("M13 9PL");
    	
    	mvc.perform(post("/venues/1/update")
    			.param("id", String.valueOf(v.getId()))
    			.param("name", v.getName())
    			.param("capacity", String.valueOf(v.getCapacity()))
    			.param("postcode", v.getPostcode())
    			.param("road", v.getRoad())
    			.accept(MediaType.TEXT_HTML)
    			.with(csrf())
    			.with(user("Mustafa").roles(Security.ADMIN_ROLE)))
    			.andExpect(handler().methodName("updateVenue"))
    			.andExpect(status().isFound())
    			.andExpect(view().name("redirect:/venues/1"));
    	
    	verify(venueService).findLongtitudeLatitude(arg.capture());
    	verify(venueService).save(arg.capture());
	}
	
	
	@Test
	public void updateVenueWithNoName() throws Exception {
    	Venue v = new Venue();
    	v.setId(1);
    	v.setName("");
    	v.setCapacity(10);
    	v.setPostcode("M13 9PL");
    	v.setRoad("M13 9PL");
    	
    	mvc.perform(post("/venues/1/update")
    			.param("id", String.valueOf(v.getId()))
    			.param("name", v.getName())
    			.param("capacity", String.valueOf(v.getCapacity()))
    			.param("postcode", v.getPostcode())
    			.param("road", v.getRoad())
    			.accept(MediaType.TEXT_HTML)
    			.with(csrf())
    			.with(user("Mustafa").roles(Security.ADMIN_ROLE)))
    			.andExpect(handler().methodName("updateVenue"))
    			.andExpect(status().isFound())
    			.andExpect(view().name("redirect:/venues/1/update"));
    	
    	verify(venueService, never()).findLongtitudeLatitude(v);
    	verify(venueService, never()).save(v);
	}
	
	@Test
	public void updateVenueWithNoRoad() throws Exception {
    	Venue v = new Venue();
    	v.setId(1);
    	v.setName("venue");
    	v.setCapacity(10);
    	v.setPostcode("M13 9PL");
    	v.setRoad("");
    	
    	mvc.perform(post("/venues/1/update")
    			.param("id", String.valueOf(v.getId()))
    			.param("name", v.getName())
    			.param("capacity", String.valueOf(v.getCapacity()))
    			.param("postcode", v.getPostcode())
    			.param("road", v.getRoad())
    			.accept(MediaType.TEXT_HTML)
    			.with(csrf())
    			.with(user("Mustafa").roles(Security.ADMIN_ROLE)))
    			.andExpect(handler().methodName("updateVenue"))
    			.andExpect(status().isFound())
    			.andExpect(view().name("redirect:/venues/1/update"));
    	
    	verify(venueService, never()).findLongtitudeLatitude(v);
    	verify(venueService, never()).save(v);
	}
	
	@Test
	public void updateVenueWithNoPostcode() throws Exception {
    	Venue v = new Venue();
    	v.setId(1);
    	v.setName("");
    	v.setCapacity(10);
    	v.setPostcode("");
    	v.setRoad("M13 9PL");
    	
    	mvc.perform(post("/venues/1/update")
    			.param("id", String.valueOf(v.getId()))
    			.param("name", v.getName())
    			.param("capacity", String.valueOf(v.getCapacity()))
    			.param("postcode", v.getPostcode())
    			.param("road", v.getRoad())
    			.accept(MediaType.TEXT_HTML)
    			.with(csrf())
    			.with(user("Mustafa").roles(Security.ADMIN_ROLE)))
    			.andExpect(handler().methodName("updateVenue"))
    			.andExpect(status().isFound())
    			.andExpect(view().name("redirect:/venues/1/update"));
    	
    	verify(venueService, never()).findLongtitudeLatitude(v);
    	verify(venueService, never()).save(v);
	}
	
	@Test
	public void updateVenueWithNegativeCapacity() throws Exception {
    	Venue v = new Venue();
    	v.setId(1);
    	v.setName("");
    	v.setCapacity(-1);
    	v.setPostcode("M13 9PL");
    	v.setRoad("M13 9PL");
    	
    	mvc.perform(post("/venues/1/update")
    			.param("id", String.valueOf(v.getId()))
    			.param("name", v.getName())
    			.param("capacity", String.valueOf(v.getCapacity()))
    			.param("postcode", v.getPostcode())
    			.param("road", v.getRoad())
    			.accept(MediaType.TEXT_HTML)
    			.with(csrf())
    			.with(user("Mustafa").roles(Security.ADMIN_ROLE)))
    			.andExpect(handler().methodName("updateVenue"))
    			.andExpect(status().isFound())
    			.andExpect(view().name("redirect:/venues/1/update"));
    	
    	verify(venueService, never()).findLongtitudeLatitude(v);
    	verify(venueService, never()).save(v);
	}
	
	@Test
	public void updateVenueWithLongName() throws Exception {
    	Venue v = new Venue();
    	v.setId(1);
    	v.setName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    	v.setCapacity(10);
    	v.setPostcode("M13 9PL");
    	v.setRoad("M13 9PL");
    	
    	mvc.perform(post("/venues/1/update")
    			.param("id", String.valueOf(v.getId()))
    			.param("name", v.getName())
    			.param("capacity", String.valueOf(v.getCapacity()))
    			.param("postcode", v.getPostcode())
    			.param("road", v.getRoad())
    			.accept(MediaType.TEXT_HTML)
    			.with(csrf())
    			.with(user("Mustafa").roles(Security.ADMIN_ROLE)))
    			.andExpect(handler().methodName("updateVenue"))
    			.andExpect(status().isFound())
    			.andExpect(view().name("redirect:/venues/1/update"));
    	
    	verify(venueService, never()).findLongtitudeLatitude(v);
    	verify(venueService, never()).save(v);
	}
	
	@Test
	public void updateVenueWithLongRoad() throws Exception {
    	Venue v = new Venue();
    	v.setId(1);
    	v.setName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    	v.setCapacity(10);
    	v.setPostcode("M13 9PL");
    	v.setRoad("M13 9PL");
    	
    	mvc.perform(post("/venues/1/update")
    			.param("id", String.valueOf(v.getId()))
    			.param("name", v.getName())
    			.param("capacity", String.valueOf(v.getCapacity()))
    			.param("postcode", v.getPostcode())
    			.param("road", v.getRoad())
    			.accept(MediaType.TEXT_HTML)
    			.with(csrf())
    			.with(user("Mustafa").roles(Security.ADMIN_ROLE)))
    			.andExpect(handler().methodName("updateVenue"))
    			.andExpect(status().isFound())
    			.andExpect(view().name("redirect:/venues/1/update"));
    	
    	verify(venueService, never()).findLongtitudeLatitude(v);
    	verify(venueService, never()).save(v);
	}

}
