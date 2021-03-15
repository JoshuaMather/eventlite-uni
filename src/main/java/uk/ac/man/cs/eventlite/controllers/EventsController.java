package uk.ac.man.cs.eventlite.controllers;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@GetMapping
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
		model.addAttribute("venues", venueService.findAll());

		return "events/index";
	}
	
	@GetMapping("/new")
	public String newEvent(Model model) {
		if (!model.containsAttribute("event")) {
			model.addAttribute("venues", venueService.findAll());
			model.addAttribute("event", new Event());
		}

		return "events/new";
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {

		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll());
			return "events/new";
		}

		event.setVenue(venueService.findById(event.getV_id()));
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "New event added.");

		return "redirect:/events";
	}
	
	@DeleteMapping("/{id}")
	public String deleteEventById(@PathVariable Long id) {
		eventService.deleteById(id);
		return "redirect:/events";
	}
	
	
	@GetMapping("/{id}")
	public String eventDescription(@PathVariable("id") long id, Model model) {	
		Event event = eventService.findById(id);
		model.addAttribute("event", event);

		return "events/show";
	}
	
	@GetMapping("/{id}/update")
	public String updateEvent(Model model, @PathVariable("id") long id) {
		model.addAttribute("event", eventService.findById(id));
		model.addAttribute("venues", venueService.findAll());
		
		return "/events/update";
	}
	
	@PostMapping("/{id}/update")
	public String saveUpdates(Model model, @PathVariable("id") long id, 
			@Valid @ModelAttribute Event event,BindingResult result) {
		
		if(result.hasErrors()) {
			return String.format("redirect:/events/%d/update", id);
		}
		
		eventService.save(event);
		
		return "redirect:/events";
	}



}
