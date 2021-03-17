package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Component
@Profile({ "default", "test" })
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (eventService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}

		// Build and save initial models here.
		Venue v1 = new Venue();
		v1.setName("Kilburn G23");
		v1.setRoad("M13 9PL");
		v1.setPostcode("M13 9PL");
		v1.setCapacity(80);
		venueService.save(v1);
		
		Venue v2 = new Venue();
		v2.setName("Online");
		v2.setRoad("-");
		v2.setPostcode("-");
		v2.setCapacity(100000);
		venueService.save(v2);
		
		Venue v3 = new Venue();
		v3.setName("Kilburn LF31");
		v3.setRoad("M13 9PL");
		v3.setPostcode("M13 9PL");
		v3.setCapacity(50);
		venueService.save(v3);
		
		Venue vC = new Venue();
		vC.setName("Venue C");
		vC.setRoad("19 Acacia Avenue");
		vC.setPostcode("WA15 8QY");
		vC.setCapacity(10);
		venueService.save(vC);
		
		Venue vB = new Venue();
		vB.setName("Venue B");
		vB.setRoad("Highland Road");
		vB.setPostcode("S43 2EZ");
		vB.setCapacity(1000);
		venueService.save(vB);
		
		Venue vA = new Venue();
		vA.setName("Venue A");
		vA.setRoad("23 Manchester Road");
		vA.setPostcode("E14 3BD");
		vA.setCapacity(50);
		venueService.save(vA);
		
		Event e1 = new Event();
		e1.setName("COMP23412 Showcase, group G");
		e1.setDate(LocalDate.of(2021, 05, 13));
		e1.setTime(LocalTime.of(16, 00));
		e1.setVenue(v1);
		eventService.save(e1);
		
		Event e2 = new Event();
		e2.setName("COMP23412 Showcase, group H");
		e2.setDate(LocalDate.of(2021, 05, 11));
		e2.setTime(LocalTime.of(11, 00));
		e2.setVenue(v2);
		eventService.save(e2);
		
		Event e3 = new Event();
		e3.setName("COMP23412 Showcase, group F");
		e3.setDate(LocalDate.of(2021, 05, 10));
		e3.setTime(LocalTime.of(16, 00));
		e3.setVenue(v2);
		eventService.save(e3);	
		
		Event e4 = new Event();
		e4.setName("COMP23412 lab");
		e4.setDate(LocalDate.of(2021, 06, 15));
		e4.setTime(LocalTime.of(12, 00));
		e4.setVenue(v3);
		eventService.save(e4);	
		
		Event eC = new Event();
		eC.setName("Event C");
		eC.setDate(LocalDate.of(2021, 02, 21));
		eC.setTime(LocalTime.of(9, 00));
		eC.setVenue(vC);
		eventService.save(eC);	
		
		Event eB = new Event();
		eB.setName("Event B");
		eB.setDate(LocalDate.of(2021, 07, 07));
		eB.setTime(LocalTime.of(15, 15));
		eB.setVenue(vB);
		eventService.save(eB);	
		
		Event eA = new Event();
		eA.setName("Event A");
		eA.setDate(LocalDate.of(2021, 04, 17));
		eA.setTime(LocalTime.of(11, 45));
		eA.setVenue(vA);
		eventService.save(eA);	
			
		
	}
}
