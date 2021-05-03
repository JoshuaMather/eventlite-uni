package uk.ac.man.cs.eventlite.controllers;

import java.time.Instant;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.TwitterService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Autowired
	private TwitterService twitterService;

	@GetMapping
	public String getAllEvents(Model model) throws TwitterException {
		Iterable<Event> events = eventService.findAllByAsc();
		model.addAttribute("events", eventService.findAll());
		model.addAttribute("eventsUpcoming", eventService.findUpcomingEvents(events));
		events = eventService.findAllByDesc();
		model.addAttribute("eventsPrevious", eventService.findPreviousEvents(events));
		model.addAttribute("venues", venueService.findAll());
		model.addAttribute("java8Instant", Instant.now());
		
		TwitterService twitter_service = new TwitterService();
		Twitter twitter = twitter_service.getTwitterInstance();
   	 	List<Status> timeline = twitter.getUserTimeline();
		model.addAttribute("latestTweets", twitter_service.getTimeLine(timeline));
		model.addAttribute("latestTweetsId", twitter_service.getTimelineId(timeline));
		model.addAttribute("latestTweetsDate", twitter_service.getTimelineDates(timeline));
		
		
		return "events/index";
	}
	
	@GetMapping("/new")
	public String newEvent(Model model) {
		if (!model.containsAttribute("event")) {
			model.addAttribute("venues", venueService.findAll());
			model.addAttribute("event", new Event());
		}

		return "events/new";
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {

		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll());
			return "events/new";
		}

		event.setVenue(venueService.findById(event.getV_id()));
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "New event added.");

		return "redirect:/events";
	}
	
	@DeleteMapping("/{id}")
	public String deleteEventById(@PathVariable Long id, RedirectAttributes redirectAttrs) {
		eventService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");
		return "redirect:/events";
	}
	
	
	@GetMapping("/{id}")
	public String eventDescription(@PathVariable("id") long id, Model model) {	
		
		if (eventService.findById(id) == null) {
			return "redirect:/events";
		}
		
		Event event = eventService.findById(id);
		model.addAttribute("event", event);
		model.addAttribute("java8Instant", Instant.now());
				
		return "events/show";
	}
	
	@GetMapping("/{id}/update")
	public String updateEvent(Model model, @PathVariable("id") long id) {
		model.addAttribute("event", eventService.findById(id));
		model.addAttribute("venues", venueService.findAll());
		
		return "/events/update";
	}
	
	@PostMapping("/{id}/update")
	public String saveUpdates(Model model, @PathVariable("id") long id, 
			@Valid @ModelAttribute Event event,BindingResult result) {
		
		if(result.hasErrors()) {
			return String.format("redirect:/events/%d/update", id);
		}
		
		eventService.save(event);
		
		return String.format("redirect:/events/%d", id);
	}

	@GetMapping("/search")
	public String searchEvent(Model model, @RequestParam(value = "search", required = true) String search) {
		Iterable<Event> events = eventService.findByNameAsc(search);	
		model.addAttribute("eventsUpcoming", eventService.findUpcomingEvents(events));
		
		events = eventService.findByNameDesc(search);
		model.addAttribute("eventsPrevious", eventService.findPreviousEvents(events));
		model.addAttribute("venues", venueService.findAll());
		model.addAttribute("java8Instant", Instant.now());
		
		return "/events/search";
	}
	
	@PostMapping("/{id}/tweet")
	public String sendTweet(Model model, @RequestParam(value = "tweet", required = true) String tweet,
			 @PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		
		tweet = tweet.strip();
		
		if(tweet.length() > 280) {
			return String.format("redirect:/events/%d", id);
		}
		
		twitterService.postTweet(tweet);
		
		redirectAttrs.addFlashAttribute("tweet", tweet);
		
		return String.format("redirect:/events/%d", id);
	}


}
