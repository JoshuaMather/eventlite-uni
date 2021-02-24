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
		Event e1 = new Event();
		e1.setId(1);
		e1.setName("COMP23412 Showcase, group G");
		e1.setDate(LocalDate.of(2021, 05, 13));
		e1.setTime(LocalTime.of(16, 00));
		e1.setVenue(2);
		eventService.save(e1);
		
		Event e2 = new Event();
		e2.setId(2);
		e2.setName("COMP23412 Showcase, group H");
		e2.setDate(LocalDate.of(2021, 05, 11));
		e2.setTime(LocalTime.of(11, 00));
		e2.setVenue(2);
		eventService.save(e2);
		
		Event e3 = new Event();
		e3.setId(3);
		e3.setName("COMP23412 Showcase, group F");
		e3.setDate(LocalDate.of(2021, 05, 10));
		e3.setTime(LocalTime.of(16, 00));
		e3.setVenue(2);
		eventService.save(e3);

	}
}
