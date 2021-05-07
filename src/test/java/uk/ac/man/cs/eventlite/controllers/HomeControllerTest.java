package uk.ac.man.cs.eventlite.controllers;

import java.util.Collections;

import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;


@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeController.class)
public class HomeControllerTest {
	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;
	
	@Test
	public void getEmptyIndex() throws Exception{
		
        when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
        when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
        
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(view().name("index")).andExpect(handler().methodName("getAllInformation"))
        	.andExpect(model().attribute("threeUpcomingEvents", Collections.<Event>emptyList()))
        	.andExpect(model().attribute("topThreeVenues", Collections.<Venue>emptyList()));
        
        verify(eventService).findAll();
        verify(venueService).findAll();
	}

}
