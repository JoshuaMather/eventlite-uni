package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";
	
	@Autowired
	private VenueRepository venueRepository;

	@Override
	public long count() {
		return venueRepository.count();
	}
	
	@Override
	public Iterable<Venue> findAllByDesc() {
		return venueRepository.findAllByOrderByNameAsc();
	}
	
	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	
	@Override
	public Venue save(Venue v) {
		return venueRepository.save(v);
	}
	
	@Override
	public Venue findById(long id) {
		return venueRepository.findById(id);
	}

	@Override
	public void deleteById(long id) {
		venueRepository.deleteById(id);
	}
	
	@Override
	public Iterable<Venue> findTopThreeVenues(Iterable<Venue> venues){
		ArrayList<Venue> temp = new ArrayList<Venue>();
		
		for (Venue v: venues) {
			temp.add(v);
		}
		
		sort(temp);
		
		if (temp.size()<3) {
			Iterable<Venue> top3 = temp;
			return  top3;
		}
		Iterable<Venue> top3 = new ArrayList<Venue>(temp.subList(temp.size() -3, temp.size()));
		return  top3;
	}
	
    public static void sort(ArrayList<Venue> list) 
    { 
  
        list.sort((v1, v2) 
                      -> Integer.compare(((List<Event>) v1.getEvents()).size(), ((List<Event>) v2.getEvents()).size())); 
    } 
}
