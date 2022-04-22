package helpingfriendlybook.service;

import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.FriendshipDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@Service
public class Unfollower {

    private static final Logger LOG = LoggerFactory.getLogger(Unfollower.class);

    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    private int startingPoint = -1;

    private final int maxShowUserRequests = 300;

    private final int maxFriendshipRequests = 15;

    private final int interval = 3;

    @Value("${unfollower.threshold}")
    private Integer followerThreshold;

    public Unfollower(GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
    }

    @Scheduled(cron="${cron.unfollow}")
    public void unfollow() {
        incrementStartingPoint();
        LOG.warn("Looking for users to unfollow...");
        try {
            List<String> unfollowed = new ArrayList<>();
            int friendshipRequests = 0;
            int showUserRequests = 0;

            List<DataDTO> friends = twitterService.getFriendsList("phishcompanion");

            for (int j = 0; j < friends.size(); j++) {
                if (friendshipRequests == maxFriendshipRequests) {
                    LOG.warn("Reached max show friendship requests " + "(" + maxFriendshipRequests + ")");
                    break;
                }
                if (showUserRequests == maxShowUserRequests) {
                    LOG.warn("Reached max show user requests " + "(" + maxShowUserRequests + ")");
                    break;
                }
                if (j % interval != startingPoint) {
                    continue;
                }
                DataDTO friend = friends.get(showUserRequests++);
                try {
                    DataDTO user = twitterService.showUser(friend.getScreenName()).getBody();
                    if (user == null) return;
                    if (user.getFollowersCount() < followerThreshold) {
                        FriendshipDTO friendship = twitterService.showFriendship(friend.getScreenName()).getBody();
                        friendshipRequests++;
                        if (friendship == null || friendship.getRelationship() == null || friendship.getRelationship().getSource() == null) return;
                        if (!friendship.getRelationship().getSource().getFollowed_by()) {
                            LOG.warn("Unfollowing " + user.getScreenName() + "...");
                            twitterService.unfollow(user);
                            unfollowed.add(user.getScreenName());
                        }
                    }
                } catch (Exception e) {
                    if (e.getMessage().contains("Too Many Requests")) {
                        googliTweeter.tweet("Hit request limit trying to unfollow");
                        tweetUnfollowedRemaining(unfollowed);
                        return;
                    }
                    googliTweeter.tweet("User " + friend.getScreenName() + " not found!");
                }
                tweetUnfollowedBatch(unfollowed);
            }
            tweetUnfollowedRemaining(unfollowed);
            LOG.warn("Finished checking users");
        } catch (Exception e) {
            googliTweeter.tweet("HFB caught exception trying to unfollow: " + e.getCause());
        }
    }

    private void incrementStartingPoint() {
        if (startingPoint >= (interval - 1)) {
            startingPoint = 0;
        } else {
            startingPoint++;
        }
    }

    private void tweetUnfollowedRemaining(List<String> unfollowed) {
        if (!unfollowed.isEmpty()) {
            tweetUnfollowed(unfollowed);
        }
    }

    private void tweetUnfollowedBatch(List<String> unfollowed) {
        if (unfollowed.size() == 4) {
            tweetUnfollowed(unfollowed);
            unfollowed.clear();
        }
    }

    private void tweetUnfollowed(List<String> unfollowed) {
        googliTweeter.tweet("PhishCompanion unfollowed " + String.join(", ", unfollowed) + " (<" + followerThreshold + ")");
    }
}