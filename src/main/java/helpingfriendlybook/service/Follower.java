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
public final class Follower {
    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    @Value("${twitter.followed.user}")
    private String followedUser;

    @Value("${twitter.followed.user.tweets}")
    private Integer followedUserTweets;

    public Follower(final GoogliTweeter googli, final TwitterService ts) {
        this.googliTweeter = googli;
        this.twitterService = ts;
    }

    @Scheduled(cron = "${cron.follow}")
    public void follow() {
        googliTweeter.tweet("Following users who liked the last " + followedUserTweets + " tweet(s) for userId: " + followedUser + "...");
        try {
            ResponseEntity<TwitterResponseDTO> responseEntity = twitterService.getTweetsForUserId(followedUser);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Integer usersFollowed = 0;
                for (int i = 0; i < followedUserTweets; i++) {
                    usersFollowed += twitterService.followFavoritesById(responseEntity.getBody().getData().get(i).getId());
                }
                googliTweeter.tweet(".@PhishCompanion requested to follow " + usersFollowed + " users at " + new Date() + ".");
            } else {
                googliTweeter.tweet("HFB was unable to fetch tweets. Did not receive 200 from twitter api call.");
            }
        } catch (Exception e) {
            googliTweeter.tweet("HFB caught exception following users: ", e);
        }
    }
}
