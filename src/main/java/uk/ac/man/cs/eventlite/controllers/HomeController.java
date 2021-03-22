package uk.ac.man.cs.eventlite.controllers;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value="/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomeController {
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	
	@GetMapping
	public String getAllInformation(Model model) {
		Iterable<Event> events = eventService.findAllByAsc();
		Iterable<Venue> venues = venueService.findAllByDesc();
		model.addAttribute("threeUpcomingEvents", eventService.findThreeUpcomingEvents(events));
		model.addAttribute("topThreeVenues", venueService.findTopThreeVenues(venues));
		
		return "index";
	}
	
}
