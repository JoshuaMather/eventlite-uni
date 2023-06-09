package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueRepository extends CrudRepository<Venue, Long>{
	
	public Venue findById(long id);
	
	public Iterable<Venue> findAllByOrderByNameAsc();
	
	public Iterable<Venue> findByNameContainsIgnoreCaseOrderByNameAsc(String name);
	
	public Iterable<Venue> findAllByNameContains(String name);
}
