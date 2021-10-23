package helpingfriendlybook.service;

import helpingfriendlybook.dto.TwitterResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
public class Follower {

    private static final Logger LOG = LoggerFactory.getLogger(Follower.class);

    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    @Value("${twitter.followed.user}")
    private String followedUser;

    public Follower(GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
    }

    @Scheduled(cron="${cron.follow}")
    public void follow() {
        LOG.warn("Following users who liked the last 5 tweets for userId: " + followedUser + "...");
        try {
            ResponseEntity<TwitterResponseDTO> responseEntity = twitterService.getTweetsForUserId(followedUser);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                for (int i = 0; i < 5; i++ ) {
                    twitterService.followFavoritesById(responseEntity.getBody().getData().get(i).getId());
                }
            } else {
                googliTweeter.tweet("HFB was unable to fetch tweets!");
            }
        } catch (Exception e) {
            googliTweeter.tweet("HFB caught exception: " + e.getCause());
        }
    }
}