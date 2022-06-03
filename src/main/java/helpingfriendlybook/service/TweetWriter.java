package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TweetWriter {
    private static final Logger LOG = LoggerFactory.getLogger(TweetWriter.class);

    @Value("${custom.hashtags}")
    private String customHashtags;

    public String addShowHashtags(String tweet) {
        return tweet + "\n\n#phish #phishstats #phishcompanion";
    }

    public String addSongHashtags(String tweet) {
        return tweet + "\n\n#phish #phishstats #phishcompanion " + customHashtags;
    }

    public String writeTweet(SongDTO songDTO, Integer bustoutThreshold) {
        if (songDTO == null) {
            return null;
        }

        String tweet = "";
        if (songDTO.getTimes() == 0) {
            tweet = songDTO.getName();
        } else {
            tweet += count(songDTO.getTimes() + 1) + " " + songDTO.getName() +
                    "\nLast played: " + songDTO.getLastPlayed() +
                    "\nGap: " + songDTO.getGap() +
                    "\nFirst played: " + songDTO.getDebut() +
                    "\n" + songDTO.getLink();
        }

        String tweetWithHashtags = addSongHashtags(tweet);
        if (songDTO.getGap() > bustoutThreshold) {
            tweetWithHashtags += " #bustout";
        }
        LOG.warn("Created tweet: " + tweetWithHashtags);

        return tweetWithHashtags;
    }

    private String count(int times) {
        if (("" + times).endsWith("11")) {
            return times + "th";
        }
        if (("" + times).endsWith("12")) {
            return times + "th";
        }
        if (("" + times).endsWith("13")) {
            return times + "th";
        }
        if (times % 10 == 1) {
            return times + "st";
        } else if (times % 10 == 2) {
            return times + "nd";
        } else if (times % 10 == 3) {
            return times + "rd";
        } else {
            return times + "th";
        }
    }
}
