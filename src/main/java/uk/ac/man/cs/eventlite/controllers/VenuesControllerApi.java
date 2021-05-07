package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

		return venueCollection(venueService.findAllByAsc());
	}
	
	private CollectionModel<Venue> venueCollection(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();
		Link profileLink = linkTo(HomeControllerApi.class).slash("profile").slash("venues").withRel("profile");
		
		return CollectionModel.of(venues, selfLink, profileLink);
	}
	
	@GetMapping("/{id}")
	public EntityModel<Venue> venueDescription(@PathVariable("id") long id) {
		Venue venue = venueService.findById(id);

		return venueToResource(venue);
	}
	
	private EntityModel<Venue> venueToResource(Venue venue) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(venue.getId()).withSelfRel();
		Link venueLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withRel("venue");
		Link eventLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).slash("events").withRel("events");
		Link next3eventsLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).slash("next3events").withRel("next3events");
		
		return EntityModel.of(venue, selfLink, venueLink, eventLink, next3eventsLink);
	}
	
	@GetMapping("/{id}/next3events")
	public CollectionModel<Event> list(@PathVariable("id") long id) {
		
		Iterable<Event> events = StreamSupport.stream(venueService.findById(id).getEvents().spliterator(), false)
				.filter(x -> x.getDate().isAfter(LocalDate.now())).sorted(Comparator.comparing(Event::getDate)).limit(3)
				.collect(Collectors.toList());
		
		return eventListToResource(events);
	}
	
	private CollectionModel<Event> eventListToResource(Iterable<Event> events) {

		return CollectionModel.of(events);

	}
} 
