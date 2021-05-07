package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
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
