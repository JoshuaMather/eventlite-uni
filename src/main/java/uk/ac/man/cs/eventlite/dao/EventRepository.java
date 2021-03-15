package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long> {
	
	public Iterable<Event> findAll();
	
	public Iterable<Event> findAllByOrderByDateAscNameAsc();
	
	public Iterable<Event> findAllByOrderByDateDescNameAsc();
	
	public long count();
	
	@SuppressWarnings("unchecked")
	public Event save(Event e);	
	

	public Event findById(long id);
}
