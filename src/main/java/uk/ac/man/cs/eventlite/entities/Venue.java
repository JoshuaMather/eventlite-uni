package uk.ac.man.cs.eventlite.entities;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Entity
public class Venue {
	
	@Id
	@GeneratedValue
	private long venue_id;

	@NotEmpty(message = "The venue name cannot be empty.")
	@Size(max = 255, message = "The venue name must have 255 characters or less.")
	private String name;

	@Min(value = 0, message = "The venue capacity must be a positive integer.")
	private int capacity;
	
	private String address;
	
	@NotEmpty(message = "The venue postcode cannot be empty.")
	private String postcode;
	
	@NotEmpty(message = "The venue road name cannot be empty.")
	@Size(max = 299, message = "The venue road name must have 299 characters or less.")
	private String road;
	

	@OneToMany(mappedBy = "venue")
	@OrderBy("date ASC, name ASC")
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

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	
	public String getPostcode() {
		return postcode;
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
	
	public String getRoad() {
		return road;
	}

	public void setRoad(String road) {
		this.road = road;
	}
	
	public Iterable<Event> getEvents() {
		return events;
	}

	public void setRoad(List<Event> events) {
		this.events = events;
	}
}
