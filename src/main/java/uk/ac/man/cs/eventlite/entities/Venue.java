package uk.ac.man.cs.eventlite.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Venue {
	
	@Id
	@GeneratedValue
	private long venue_id;

	private String name;

	private int capacity;
	
	@OneToMany()
	private List<Event> events;

	public Venue() {
	}

	public long getId() {
		return venue_id;
	}

	public void setId(long id) {
		this.venue_id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
}
