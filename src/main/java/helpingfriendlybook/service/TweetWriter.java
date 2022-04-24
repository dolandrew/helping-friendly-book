package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class TweetWriter {
    private static final Logger LOG = LoggerFactory.getLogger(TweetWriter.class);

    @Value("${custom.hashtags}")
    private String customHashtags;

    public String addRandomShowHashtags(String tweet) {
        return tweet + "\n\n#phish #phishstats #phishcompanion #livephish #randomshow";
    }

    public String addShowHashtags(String tweet) {
        return tweet + "\n\n#phish #phishstats #phishcompanion #livephish #otd" + OffsetDateTime.now().getYear();
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
            tweet = "DEBUT: " + songDTO.getName();
        } else {
            if (songDTO.getGap() > bustoutThreshold) {
                tweet = "BUSTOUT: ";
            }
            tweet += count(songDTO.getTimes() + 1) + " " + songDTO.getName() +
                    "\nLast played: " + songDTO.getLastPlayed() +
                    "\nGap: " + songDTO.getGap() +
                    "\nFirst played: " + songDTO.getDebut() +
                    "\n" + songDTO.getLink();
        }

        String tweetWithHashtags = addSongHashtags(tweet);
        LOG.warn("Created tweet: " + tweetWithHashtags);

        return tweetWithHashtags;
    }

    private String count(int times) {
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
