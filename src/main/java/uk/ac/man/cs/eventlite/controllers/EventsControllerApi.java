package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;

@RestController
@RequestMapping(value = "/api/events", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class EventsControllerApi {

	@Autowired
	private EventService eventService;

	@GetMapping
	public CollectionModel<Event> getAllEvents() {

		return eventCollection(eventService.findAll());
	}

	// may not be needed
	private EntityModel<Event> singleEvent(Event event) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(event.getId()).withSelfRel();

		return EntityModel.of(event, selfLink);
	}

	private CollectionModel<Event> eventCollection(Iterable<Event> events) {
		Link selfLink = linkTo(methodOn(EventsControllerApi.class).getAllEvents()).withSelfRel();

		return CollectionModel.of(events, selfLink);
	}
	
	
	@GetMapping("/new")
	public ResponseEntity<?> newEvent() {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
	}
	
	@GetMapping("/{id}")
	public EntityModel<Event> eventDescription(@PathVariable("id") long id) {
		Event event = eventService.findById(id);
		
		return eventToResource(event);
	}
	
	private EntityModel<Event> eventToResource(Event event) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(event.getId()).withSelfRel();
		Link eventLink = linkTo(EventsControllerApi.class).slash(event.getId()).withRel("event");
		Link venueLink = linkTo(VenuesControllerApi.class).slash(event.getVenue().getId()).withRel("venue");

		return EntityModel.of(event, selfLink, eventLink, venueLink);
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createEvent(@RequestBody @Valid Event event, BindingResult result) {

		if (result.hasErrors()) {
			return ResponseEntity.unprocessableEntity().build();
		}

		eventService.save(event);
		URI location = linkTo(EventsControllerApi.class).slash(event.getId()).toUri();

		return ResponseEntity.created(location).build();
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteEventById(@PathVariable long id) {
		eventService.deleteById(id);
		
		return ResponseEntity.noContent().build();
	}
	
	
	@PostMapping("/{id}/update")
	public ResponseEntity<?> updateEvent(@PathVariable("id") long id, 
			@Valid @ModelAttribute Event event, BindingResult result) {
		if(result.hasErrors()) {
			return ResponseEntity.badRequest().build();
		}
		
		eventService.save(event);
		
		return ResponseEntity.ok().build();
	}
	
	
	@GetMapping("/search")
	public CollectionModel<Event> searchEvents(@RequestParam(value = "search", required = false) String search) {
		Iterable<Event> event = eventService.findByNameAsc(search);

		return eventCollection(event);
	}
} 
