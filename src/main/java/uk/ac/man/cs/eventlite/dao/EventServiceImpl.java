package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.time.LocalDate;  

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {
	
	@Autowired
	private EventRepository eventRepository;
	
	@Override
	public long count() {
		return eventRepository.count();
	}
	
	@Override
	public Iterable<Event> findAllByAsc() {
		return eventRepository.findAllByOrderByDateAscNameAsc();
	}
	
	@Override
	public Iterable<Event> findAllByDesc() {
		return eventRepository.findAllByOrderByDateDescNameAsc();
	}
	
	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAll();
	}
	
	@Override
	public Iterable<Event> findByNameAsc(String name) {
		return eventRepository.findByNameContainsIgnoreCaseOrderByDateAscNameAsc(name);
	}
	
	@Override
	public Iterable<Event> findByNameDesc(String name) {
		return eventRepository.findByNameContainsIgnoreCaseOrderByDateDescNameAsc(name);
	}
	
	@Override
	public Iterable<Event> findUpcomingEvents(Iterable<Event> events){
		
		ArrayList<Event> Events = new ArrayList<Event>();
		
		LocalDate now = LocalDate.now();
		
		for (Event e: events) {
			if (e.getDate().isAfter(now) || e.getDate().equals(now)) {
				Events.add(e);
			}
		}
		
		Iterable<Event> UpcomingEvents = Events;
		return UpcomingEvents;
	}
	
	@Override
	public Iterable<Event> findPreviousEvents(Iterable<Event> events){
		
		ArrayList<Event> Events = new ArrayList<Event>();
		
		LocalDate now = LocalDate.now();
		
		for (Event e: events) {
			if (e.getDate().isBefore(now) || e.getDate().equals(now)) {
				Events.add(e);
			}
		}
		
		Iterable<Event> UpcomingEvents = Events;
		return UpcomingEvents;
	}
	
	@Override
	public Event save(Event e) {
		return eventRepository.save(e);
	}
	
	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}
	
	@Override
	public Event findById(long id) {
		return eventRepository.findById(id);
	}
	
	@Override
	public Iterable<Event> findByName(String name) {
		return eventRepository.findAllByNameContains(name);
	}
	
}
