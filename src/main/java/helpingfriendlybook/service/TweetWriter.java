package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TweetWriter {

    private static final Logger LOG = LoggerFactory.getLogger(TweetWriter.class);

    public String writeTweet(SongDTO songDTO, Integer bustoutThreshold, String customHashtags) {
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

        String tweetWithHashtags = addHashtags(tweet, customHashtags);
        LOG.warn("Created tweet: " + tweetWithHashtags);

        return tweetWithHashtags;
    }

    private String addHashtags(String tweet, String customHashtags) {
        return tweet + "\n\n#phish #phishstats #phishcompanion #livephish #phishfromtheroad " + customHashtags;
    }
}
