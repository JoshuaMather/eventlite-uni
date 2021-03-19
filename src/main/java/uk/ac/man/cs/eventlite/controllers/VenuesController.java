package uk.ac.man.cs.eventlite.controllers;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.bytebuddy.asm.Advice.OffsetMapping.Sort;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;

	@GetMapping
	public String getAllVenues(Model model) {

		model.addAttribute("venues", venueService.findAllByDesc());

		return "venues/index";
	}
	
	@GetMapping("/new")
	public String newVenue(Model model) {
		if (!model.containsAttribute("venue")) {
			model.addAttribute("venue", new Venue());
		}

		return "venues/new";
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createVenue(@RequestBody @Valid @ModelAttribute Venue venue, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {

		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
			return "venues/new";
		}

		venue.setAddress(venue.getRoad() + ", " + venue.getPostcode());
		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "New venue added.");

		return "redirect:/venues";
	}
	
	@GetMapping("/{id}")
	public String venueDescription(@PathVariable("id") long id, Model model) {	
		Venue venue = venueService.findById(id);
		model.addAttribute("venue", venue);	
		model.addAttribute("eventsUpcoming", eventService.findUpcomingEvents(venue.getEvents()));

		return "venues/show";
	}
	
	@DeleteMapping("/{id}")
	public String deleteVenueById(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
			
		venueService.deleteById(id);
			
		return "redirect:/venues";
		
	}
	

}
