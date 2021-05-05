package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


@Service
public class TwitterService {

    private Twitter twitter;

    public TwitterService() {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setDebugEnabled(true)
        .setOAuthConsumerKey("zYzyEyoqxFKGrVfP3GdisWoln")
        .setOAuthConsumerSecret("hOgFIc5AhUpCnBmOCdkuHT3tJV9YJfGaEN9DDcrrNJ2z2RT0VE")
        .setOAuthAccessToken("1381630436909199360-O7TL8e6XtViU6tpDXV7aQHFLfOOdfd")
        .setOAuthAccessTokenSecret("xVn2vdmCHTVWkYRGfsKBpqJbR80HZBzzcZrj3HO7iDnen");
        TwitterFactory tf = new TwitterFactory(configBuilder.build());
        twitter = tf.getInstance();
    }


    public void postTweet(String tweet) {
    	try {
    		twitter.updateStatus(tweet);
    	}
    	catch(TwitterException te) {
    		te.printStackTrace();
    	}
    }

    public List<String> getTimeLine(List<Status> timeline) throws TwitterException{
    	List<String> timelineS = timeline.stream().map(item -> item.getText()).collect(Collectors.toList());
    	 if (timeline.size() > 5) {
    		 return timelineS.subList(0, 5);
    	 } else {
    		 return timelineS;
    	 }
    	 
    }
    
    public List<Long> getTimelineId(List<Status> timeline) throws TwitterException{
    	List<Long> tweetid = new ArrayList<Long>();;
    	for(int i=0; i<timeline.size(); i++) {
    		tweetid.add(timeline.get(i).getId());
    	}
    	
    	return tweetid;
    }
    
    public List<String> getTimelineDates(List<Status> timeline) throws TwitterException{
    	List<String> tweetDate = timeline.stream().map(item -> item.getCreatedAt().toString()).collect(Collectors.toList());
   	 	if (tweetDate.size() > 5) {
   	 		return tweetDate.subList(0, 5);
   	 	} else {
   	 		return tweetDate;
   	 	}
    
    }
   
    public Twitter getTwitterInstance() {	
		return twitter;
	}

    
}