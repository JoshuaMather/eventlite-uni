package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.time.LocalTime;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventService {

	public long count();
	
	Iterable<Event> findByVenue(Venue venue);

	public Iterable<Event> findAll();
	
	public Iterable<Event> findAllByAsc();
	
	public Iterable<Event> findAllByDesc();
	
	public Iterable<Event> findUpcomingEvents(Iterable<Event> events, LocalDate now);
	
	public Iterable<Event> findPreviousEvents(Iterable<Event> events, LocalDate now);
	
	public Iterable<Event> findByNameAsc(String name);
	
	public Iterable<Event> findByNameDesc(String name);
	
	public Event save(Event e);

	public void deleteById(long id);
	
	public Event findById(long id);
	
	public Iterable<Event> findByName(String id);
	
}

