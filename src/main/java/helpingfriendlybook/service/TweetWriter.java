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
            tweet += songDTO.getName() + " has been played " + songDTO.getTimes() + " times" +
                    "\nLast played: " + songDTO.getLastPlayed() +
                    "\nShow gap: " + songDTO.getGap() +
                    "\nFirst played on: " + songDTO.getDebut() +
                    "\n" + songDTO.getLink();
        }

        String tweetWithHashtags = addSongHashtags(tweet);
        LOG.warn("Created tweet: " + tweetWithHashtags);

        return tweetWithHashtags;
    }

    public String addSongHashtags(String tweet) {
        return tweet + "\n\n#phish #phishstats #phishcompanion #livephish #phishfromtheroad " + customHashtags;
    }

    public String addShowHashtags(String tweet) {
        return tweet + "\n\n#phish #phishstats #phishcompanion #livephish #otd" + OffsetDateTime.now().getYear();
    }

    public String addRandomShowHashtags(String tweet) {
        return tweet + "\n\n#phish #phishstats #phishcompanion #livephish #randomshow";
    }
}
