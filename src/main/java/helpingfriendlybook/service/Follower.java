package helpingfriendlybook.service;

import helpingfriendlybook.dto.TwitterResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@EnableScheduling
@Service
public class Follower {

    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    @Value("${twitter.followed.user}")
    private String followedUser;

    @Value("${twitter.followed.user.tweets}")
    private Integer followedUserTweets;

    public Follower(GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
    }

    @Scheduled(cron="${cron.follow}")
    public void follow() {
        googliTweeter.tweet("Following users who liked the last " + followedUserTweets + " tweets for userId: " + followedUser + "...");
        try {
            ResponseEntity<TwitterResponseDTO> responseEntity = twitterService.getTweetsForUserId(followedUser);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Integer usersFollowed = 0;
                for (int i = 0; i < followedUserTweets; i++ ) {
                    usersFollowed += twitterService.followFavoritesById(responseEntity.getBody().getData().get(i).getId());
                }
                googliTweeter.tweet("PhishCompanion followed " + usersFollowed + " users at " + new Date() + ".");
            } else {
                googliTweeter.tweet("HFB was unable to fetch tweets!");
            }
        } catch (Exception e) {
            googliTweeter.tweet("HFB caught exception: " + e.getCause());
        }
    }
}