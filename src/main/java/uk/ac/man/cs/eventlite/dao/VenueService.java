package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public Iterable<Venue> findAllByDesc();
	
    @SuppressWarnings("unchecked")
	public Venue save(Venue v);
    
    public Venue findById(long id);

	public void deleteById(long id);
	
	public Iterable<Venue> findTopThreeVenues(Iterable<Venue> venues);
}
