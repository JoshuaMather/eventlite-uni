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

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
	public Iterable<Venue> findAllByAsc() {
		return venueRepository.findAllByOrderByNameAsc();
	}
	
	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	@Override
	public Iterable<Venue> findByNameAsc(String name) {
		return venueRepository.findByNameContainsIgnoreCaseOrderByNameAsc(name);
	}
	
	@Override
	public Venue save(Venue v) {
		//Set coordinates for venue here
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
	
    public static void sort(ArrayList<Venue> list) 
    { 
  
        list.sort((v1, v2) 
                      -> Integer.compare(((List<Event>) v1.getEvents()).size(), ((List<Event>) v2.getEvents()).size())); 
    }
    
    
    //make sure query is the postcode to remove any ambiguity
    @Override
    public void findLongtitudeLatitude(Venue venue){
    	String query = venue.getPostcode();
//    	query = query.replaceAll("\\s+","");
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken("pk.eyJ1IjoiZXZlbnRsaXRlZjEiLCJhIjoiY2tta3hjb3VmMDduMzJ4cnpxdDVwa2M0eSJ9.Qn8ih-E9sJje_-XZw9gbEQ")
				.query(query)
				.build();
		
		mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
			@Override
			public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
		 
				List<CarmenFeature> results = response.body().features();
		 
				if (results.size() > 0) {
		 
				  // Log the first results Point.
				  Point p = results.get(0).center();
				  venue.setLatitude(p.latitude());
				  venue.setLongitude(p.longitude());
				  venueRepository.save(venue);
//				  Log.d(TAG, "onResponse: " + firstResultPoint.toString());
		 
//				} else {
		 
				  // No result for your request were found.
//				  Log.d(TAG, "onResponse: No result found");
		 
				}
			}
		 
			@Override
			public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
				throwable.printStackTrace();
			}
		});
    }
}
