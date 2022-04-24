package helpingfriendlybook.service;

import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static helpingfriendlybook.service.TwitterService.getSomeHoursAgo;

@EnableScheduling
@Service
public class Favoriter {

    private static final Logger LOG = LoggerFactory.getLogger(Favoriter.class);

    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    @Value("${favoriter.max}")
    private Integer maxTweetsLiked;

    @Value("${favoriter.interval.hours}")
    private Integer intervalHours;

    private final List<String> checkedIds = new ArrayList<>();
    private final List<String> userIdsToFavorite = List.of("2237218753", "145023741", "2202143780", "1492957487888281607", "3378157977", "1441291459018121220", "232312841", "14503997");
    private final List<String> screenNamesToFavorite = List.of("PhishtoryToday", "secretcabdriver", "PhishatMSG", "PhishRT", "PhishJustJams", "StadiumTourLife", "LivePhish", "Phish");

    public Favoriter(GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
    }

    @Scheduled(cron="${cron.favorite}")
    public void listenToPhishFTR() {
        for (int i = 0; i < userIdsToFavorite.size(); i++) {
            int tweetsLiked = 0;
            try {
                var tweets = twitterService.getTweetsAndRetweetsForUserIdInLast(userIdsToFavorite.get(i), getSomeHoursAgo(intervalHours));
                TwitterResponseDTO body = tweets.getBody();
                if (body != null) {
                    if (body.getData() != null) {
                        for (DataDTO tweet : body.getData()) {
                            if (tweetsLiked == maxTweetsLiked) {
                                break;
                            }
                            if (!checkedIds.contains(tweet.getId())) {
                                LOG.warn("Found new tweet.");
                                String tweetId = tweet.getId();
                                twitterService.favoriteTweetById(tweetId);
                                tweetsLiked++;
                                checkedIds.add(tweetId);
                                googliTweeter.tweet("HFB liked " + screenNamesToFavorite.get(i) +  "'s tweet: \"" + tweet.getText() + "\"");
                            }
                        }
                    } else {
                        LOG.warn("HFB found no tweets in given time period.");
                    }
                }
            } catch (Exception e) {
                googliTweeter.tweet("HFB caught exception: " + e.getMessage());
            }
        }
    }
}