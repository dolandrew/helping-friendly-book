package helpingfriendlybook.service;

import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.SongDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Locale;

import static helpingfriendlybook.service.TwitterService.getOneMinuteAgo;
import static java.lang.Integer.parseInt;

@EnableScheduling
@Service
public class TweetListener {
    private static final Logger LOG = LoggerFactory.getLogger(TweetListener.class);

    private final GoogliTweeter googliTweeter;

    private final MetadataAssembler metadataAssembler;

    private final TweetWriter tweetWriter;

    private final TwitterService twitterService;

    @Value("${bustout.threshold}")
    private Integer bustoutThreshold;

    private String currentSongName;

    @Value("${ignored.song}")
    private String ignoredSong;

    @Value("${twitter.phish.ftr.id}")
    private String phishFTRid;

    private final TimeApiService timeApiService;

    public TweetListener(MetadataAssembler metadataAssembler, TweetWriter tweetWriter, GoogliTweeter googliTweeter,
                         TwitterService twitterService, TimeApiService timeApiService) {
        this.metadataAssembler = metadataAssembler;
        this.tweetWriter = tweetWriter;
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
        this.timeApiService = timeApiService;
    }

    @Scheduled(cron = "${cron.listen}")
    public void listenToPhishFTR() {
        try {
            var tweets = twitterService.getTweetsForUserIdInLast(phishFTRid, getOneMinuteAgo());
            String songName = processIncomingTweet(tweets);
            if (songName != null) {
                if (songName.equals(ignoredSong)) {
                    googliTweeter.tweet("Ignored expected song: " + ignoredSong);
                } else {
                    processOutgoingTweet(songName);
                }
            }
        } catch (Exception e) {
            googliTweeter.tweet("HFB caught exception: " + e.getMessage());
        }
    }

    public static String cleanSongName(String fetchedSongName) {
        return fetchedSongName
                .replaceAll("&gt; ", "")
                .replaceAll("&gt;", "")
                .replaceAll("> ", "")
                .replaceAll(">", "")
                .replaceAll("SET ONE: ", "")
                .replaceAll("SET TWO: ", "")
                .replaceAll("SET THREE: ", "")
                .replaceAll("SET FOUR: ", "")
                .replaceAll("SET FIVE: ", "")
                .replaceAll("ENCORE TWO: ", "")
                .replaceAll("ENCORE: ", "");
    }

    private String processIncomingTweet(ResponseEntity<TwitterResponseDTO> responseEntity) {
        String cleanedSongName = null;
        TwitterResponseDTO body = responseEntity.getBody();
        if (body != null) {
            if (body.getData() != null) {
                DataDTO data = body.getData().get(0);
                String fetchedSongName = data.getText();
                cleanedSongName = cleanSongName(fetchedSongName);
                if (sameTweet(cleanedSongName)) {
                    LOG.warn("Found no new tweets.");
                    return null;
                }
                LOG.warn("Found new tweet.");
                currentSongName = cleanedSongName;
                String tweetId = responseEntity.getBody().getData().get(0).getId();
                twitterService.favoriteTweetById(tweetId);
                if (shouldIgnoreTweet(fetchedSongName)) {
                    return null;
                }
                try {
                    checkForSetStart(fetchedSongName);
                } catch (Exception e) {
                    googliTweeter.tweet("Failed to tweet set start!");
                }
            } else {
                LOG.warn("HFB found no tweets in given time period.");
            }
        }
        return cleanedSongName;
    }

    private void processOutgoingTweet(String songName) {
        SongDTO songDTO = metadataAssembler.assembleMetadata(songName);
        String tweet = tweetWriter.writeTweet(songDTO, bustoutThreshold);
        twitterService.tweet(tweet);
    }

    private boolean sameTweet(String fetchedSongName) {
        return fetchedSongName.equals(currentSongName);
    }

    private boolean shouldIgnoreTweet(String fetchedSongName) {
        if (fetchedSongName == null) {
            LOG.warn("Skipping empty tweet");
            return true;
        }
        if (fetchedSongName.contains("\uD83D\uDCF8")
                || fetchedSongName.contains("@rene_huemer")
                || fetchedSongName.contains(">>")
                || fetchedSongName.contains("https://t.co")
                || fetchedSongName.matches(".*[0-9]+/[0-9]+/[0-9]{4}.*")
        ) {
            LOG.warn("Skipping tweet: " + fetchedSongName);
            return true;
        }
        if (fetchedSongName.startsWith("RT ")) {
            LOG.warn("Skipping retweet: " + fetchedSongName);
            return true;
        }
        return false;
    }

    public void checkForSetStart(String tweet) {
        if (tweet.contains("SET ONE:") || tweet.toLowerCase(Locale.ROOT).contains("set one:")) {
            tweetSetStart("SET ONE", "\uD83C\uDF89");
        } else if (tweet.contains("SET TWO:") || tweet.toLowerCase(Locale.ROOT).contains("set two:")) {
            tweetSetStart("SET TWO", "\uD83D\uDC20");
        } else if (tweet.contains("SET THREE:") || tweet.toLowerCase(Locale.ROOT).contains("set three:")) {
            tweetSetStart("SET THREE", "\uD83D\uDD7A");
        } else if (tweet.contains("ENCORE:") || tweet.toLowerCase(Locale.ROOT).contains("encore:")) {
            tweetSetStart("ENCORE", "⭕️");
        }
    }

    private void tweetSetStart(String setName, String emoji) {
        String timeInNewYork = timeApiService.getTimeInNewYork();
        String[] timeParts = timeInNewYork.split(":");
        int hour = parseInt(timeParts[0]) % 12;
        if (hour ==0) hour = 12;
        twitterService.tweet(emoji + " " + setName + " started at " + hour + ":" + timeParts[1] + (parseInt(timeParts[0]) >= 12 ? " PM" : " AM") + " ET");
    }
}