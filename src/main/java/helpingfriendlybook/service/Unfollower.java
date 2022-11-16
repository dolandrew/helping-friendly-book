package helpingfriendlybook.service;

import helpingfriendlybook.dto.DataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@EnableScheduling
@Service
public class Unfollower {
    private static final Logger LOG = LoggerFactory.getLogger(Unfollower.class);

    private final GoogliTweeter googliTweeter;

    private final int maxShowUserRequests = 300;

    private final TwitterService twitterService;

    private final List<String> checkedUsers = new ArrayList<>();

    @Value("${unfollower.threshold}")
    private Integer followerThreshold;

    public Unfollower(GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
    }

    @Scheduled(cron = "${cron.unfollow}")
    public void unfollow() {
        LOG.warn("Looking for users to unfollow...");
        List<String> unfollowed = new ArrayList<>();
        try {
            int showUserRequests = 0;

            String myUserName = "PhishCompanion";
            var friends = twitterService.getFriendsList(myUserName).stream()
                    .map(DataDTO::getScreenName)
                    .toList();
            var followers = twitterService.getFollowersList(myUserName).stream()
                    .map(DataDTO::getScreenName)
                    .toList();
            LOG.warn("Ratio: " + ((double) followers.size() / (double) friends.size()));

            for (String friend : friends) {
                if (showUserRequests == maxShowUserRequests) {
                    LOG.warn("Reached max show user requests " + "(" + maxShowUserRequests + ")");
                    break;
                }
                if (!followers.contains(friend) && !checkedUsers.contains(friend)) {
                    DataDTO user = twitterService.showUser(friend).getBody();
                    checkedUsers.add(friend);
                    showUserRequests++;
                    if (user != null && user.getFollowersCount() < followerThreshold) {
                        LOG.warn("Unfollowing " + user.getScreenName() + "...");
                        twitterService.unfollow(user);
                        unfollowed.add(user.getScreenName());
                    }
                }
                tweetUnfollowedBatch(unfollowed);
            }
            LOG.warn("Finished checking users");
            LOG.warn("Ratio: " + ((double) followers.size() / ((double) friends.size() - (double) unfollowed.size())));
        } catch (Exception e) {
            googliTweeter.tweet("HFB caught exception trying to unfollow: " + e.getCause());
        }
        tweetUnfollowedRemaining(unfollowed);
    }

    private void tweetUnfollowed(List<String> unfollowed) {
        googliTweeter.tweet("PhishCompanion unfollowed @" + String.join(", @", unfollowed) + " (< " + followerThreshold + " followers)");
    }

    private void tweetUnfollowedBatch(List<String> unfollowed) {
        if (unfollowed.size() == 4) {
            tweetUnfollowed(unfollowed);
            unfollowed.clear();
        }
    }

    private void tweetUnfollowedRemaining(List<String> unfollowed) {
        if (!unfollowed.isEmpty()) {
            tweetUnfollowed(unfollowed);
        }
    }
}