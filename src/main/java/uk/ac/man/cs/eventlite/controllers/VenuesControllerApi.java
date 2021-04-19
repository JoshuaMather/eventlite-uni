package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.Collection;

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
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	@Autowired
	private VenueService venueService;

	@GetMapping
	public CollectionModel<Venue> getAllVenues() {

		return venueCollection(venueService.findAllByDesc());
	}
	
	private CollectionModel<Venue> venueCollection(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();

		return CollectionModel.of(venues, selfLink);
	}
	
	@GetMapping("/new")
	public ResponseEntity<?> newVenue() {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createVenue(@RequestBody @Valid Venue venue, BindingResult result) {

		if (result.hasErrors()) {
			return ResponseEntity.unprocessableEntity().build();
		}

		venueService.save(venue);
		URI location = linkTo(VenuesControllerApi.class).slash(venue.getId()).toUri();

		return ResponseEntity.created(location).build();
	}
	
	@GetMapping("/{id}")
	public EntityModel<Venue> venueDescription(@PathVariable("id") long id) {
		Venue venue = venueService.findById(id);

		return venueToResource(venue);
	}
	
	private EntityModel<Venue> venueToResource(Venue venue) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(venue.getId()).withSelfRel();

		return EntityModel.of(venue, selfLink);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteVenueById(@PathVariable long id) {
		Venue venue = venueService.findById(id);
		Iterable<Event> events = venue.getEvents();
		
		int count = ((Collection<?>) events).size();
		
		if (count == 0) {
			venueService.deleteById(id);
			
		}
		else {
			return ResponseEntity.badRequest().build();
		}
		
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/{id}/update")
	public ResponseEntity<?> updateVenueById(@PathVariable("id") long id, 
			@RequestBody @Valid @ModelAttribute Venue venue, BindingResult result) {
		
		if(result.hasErrors()) {
			return ResponseEntity.badRequest().build();
		}
		
		venueService.save(venue);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/search")
	public CollectionModel<Venue> searchVenues(@RequestParam(value = "search", required = false) String search) {
		Iterable<Venue> venues = venueService.findByNameAsc(search);

		return venueCollection(venues);
	}
} 
