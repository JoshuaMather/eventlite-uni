package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Iterable<Event> findAllByAsc();
	
	public Iterable<Event> findAllByDesc();
	
	public Iterable<Event> findUpcomingEvents(Iterable<Event> events);
	
	public Iterable<Event> findPreviousEvents(Iterable<Event> events);
	
	public Event save(Event e);

	public void deleteById(long id);
	
	public Event findById(long id);
}
