package uk.ac.man.cs.eventlite.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.controllers.VenuesController;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
//@Disabled
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(VenuesController.class)
//@Import(Security.class)
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;
	
	@Mock
	Venue venue;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	// This class is here as a starter for testing any custom methods within the
	// VenueService. Note: It is currently @Disabled!
	
	@Test
	public void testCorrectAddress() throws Exception{	
		Venue venue = new Venue();
		venue.setId(1);
		venue.setName("venue");
		venue.setCapacity(20);
		venue.setRoad("4 Oxford Road");
		venue.setPostcode("M13 9PL");
		
		venueService.findLongtitudeLatitude(venue);
		
		assertEquals(venue.getLatitude(), 53.476429);
		assertEquals(venue.getLongitude(), -2.23217);
	}
	
	
	@Test
	public void testIncorrectAddress() throws Exception{		
		Venue venue = new Venue();
		venue.setId(1);
		venue.setName("venue");
		venue.setCapacity(20);
		venue.setRoad("4 Oxford Road");
		venue.setPostcode("ASFGABH");
		
		venueService.findLongtitudeLatitude(venue);
		
		assertEquals(venue.getLatitude(), 0);
		assertEquals(venue.getLongitude(), 0);
	}
	
	
	@Test
	public void testNoAddress() throws Exception{		
		Venue venue = new Venue();
		venue.setId(1);
		venue.setName("venue");
		venue.setCapacity(20);
		venue.setRoad("4 Oxford Road");
		venue.setPostcode("");
		
		venueService.findLongtitudeLatitude(venue);
		
		assertEquals(venue.getLatitude(), 0);
		assertEquals(venue.getLongitude(), 0);
	}
}
