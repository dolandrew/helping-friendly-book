package helpingfriendlybook.service;

import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public final class MentionService {
    private static final Logger LOG = LoggerFactory.getLogger(MentionService.class);

    private final TwitterService twitterService;

    private final PhishDotNetProxyService phishDotNetProxyService;

    private final OnThisDayService onThisDayService;

    private final String PHISH_COMPANION_USER_ID = "1435725956371550213";

    public MentionService(final TwitterService ts, final PhishDotNetProxyService proxyService, final OnThisDayService otdService) {
        this.twitterService = ts;
        this.phishDotNetProxyService = proxyService;
        this.onThisDayService = otdService;
    }

    @Scheduled(cron = "${cron.mentions}")
    public void checkForMentions() {
        LOG.info("Checking for mentions...");
        ResponseEntity<TwitterResponseDTO> mentions = twitterService.getMentionsForUserIdInLast(PHISH_COMPANION_USER_ID, TwitterService.getThirtySecondsAgo());
        if (mentions.getBody() != null && mentions.getBody().getData() != null) {
            for (DataDTO tweet : mentions.getBody().getData()) {
                String[] dateParts;
                Pattern pattern;
                Matcher matcher;
                if (tweet.getText().matches(".*[0-9]+/[0-9]+/[0-9]{4}.*")) {
                    pattern = Pattern.compile("[0-9]+/[0-9]+/[0-9]{4}");
                } else if (tweet.getText().matches(".*[0-9]+/[0-9]+/[0-9]{2}.*")) {
                    pattern = Pattern.compile("[0-9]+/[0-9]+/[0-9]{2}");
                } else {
                    continue;
                }
                LOG.warn("Found a show date mentioned.");
                twitterService.favoriteTweetById(tweet.getId());
                matcher = pattern.matcher(tweet.getText());
                if (matcher.find()) {
                    dateParts = matcher.group(0).split("/");
                    List<Element> shows = phishDotNetProxyService.getShowsForDate(Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[2]));
                    String username = "@" + twitterService.getUserById(tweet.getAuthor_id()).getData().getUsername() + " ";
                    if (!isEmpty(shows)) {
                        int random = new Random().nextInt(shows.size());
                        Element show = shows.get(random);
                        onThisDayService.tweetOnThisDay(show, tweet.getId(), username);
                    } else {
                        twitterService.tweet(username + "Helping Friendly Bot was unable to find a show on this date on phish.net.", tweet.getId());
                    }
                }
            }
        }
    }
}
