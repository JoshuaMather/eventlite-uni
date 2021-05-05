package uk.ac.man.cs.eventlite.controllers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
		Iterable<Event> events = StreamSupport.stream(eventService.findAll().spliterator(), false)
				.filter(x -> x.getDate().isAfter(LocalDate.now())).sorted(Comparator.comparing(Event::getDate)).limit(3)
				.collect(Collectors.toList());

		Comparator<Venue> venueComparator = Comparator.comparing(x -> StreamSupport.stream(eventService.findByVenue(x).spliterator(), false)
				.filter(y -> y.getDate().isAfter(LocalDate.now()))
				.count());

		Iterable<Venue> venues = StreamSupport.stream(venueService.findAll().spliterator(), false)
				.sorted(venueComparator.reversed()).limit(3).collect(Collectors.toList());

		model.addAttribute("threeUpcomingEvents", events);
		model.addAttribute("topThreeVenues", venues);
		
		return "index";
	}
	
}
