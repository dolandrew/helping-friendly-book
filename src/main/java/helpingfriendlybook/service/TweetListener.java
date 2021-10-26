package helpingfriendlybook.service;

import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.SongDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
public class TweetListener {

    private static final Logger LOG = LoggerFactory.getLogger(TweetListener.class);

    private final MetadataAssembler metadataAssembler;

    private final TweetWriter tweetWriter;

    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    @Value("${one.time.song}")
    private String oneTimeSong;

    @Value("${bustout.threshold}")
    private Integer bustoutThreshold;

    @Value("${custom.hashtags}")
    private String customHashtags;

    @Value("${cron.listen}")
    private String cron;

    @Value("${ignored.song}")
    private String ignoredSong;

    @Value("${twitter.phish.ftr.id}")
    private String phishFTRid;

    private String currentSongName;

    private boolean tweetedConfigs;

    public TweetListener(MetadataAssembler metadataAssembler, TweetWriter tweetWriter, GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.metadataAssembler = metadataAssembler;
        this.tweetWriter = tweetWriter;
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
    }

    @Scheduled(cron="${cron.listen}")
    public void listenToPhishFTR() {
        try {
            tweetPropertiesOnStartup();
            processOneTimeSong();

            ResponseEntity<TwitterResponseDTO> responseEntity = twitterService.getTweetsForUserIdInLastFiveMinutes(phishFTRid);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String songName = processIncomingTweet(responseEntity);
                if (songName != null) {
                    if (songName.equals(ignoredSong)) {
                        googliTweeter.tweet("Ignored expected song: " + ignoredSong);
                    } else {
                        processOutgoingTweet(songName);
                    }
                }
            } else {
                googliTweeter.tweet("HFB was unable to fetch tweets!");
            }
        } catch (Exception e) {
            googliTweeter.tweet("HFB caught exception: " + e.getCause());
        }
    }

    private void processOutgoingTweet(String songName) {
        SongDTO songDTO = metadataAssembler.assembleMetadata(songName);
        String tweet = tweetWriter.writeTweet(songDTO, bustoutThreshold);
        twitterService.tweet(tweet);
    }

    private void processOneTimeSong() {
        if (StringUtils.isNotBlank(oneTimeSong)) {
            googliTweeter.tweet("Found one time song: " + oneTimeSong);
            processOutgoingTweet(oneTimeSong);
            oneTimeSong = null;
        }
    }

    private void tweetPropertiesOnStartup() {
        if (!tweetedConfigs) {
            String tweet = "HFB started successfully";
            if (bustoutThreshold != null) {
                tweet += "\nbustout.threshold=" + bustoutThreshold;
            }
            if (StringUtils.isNotBlank(customHashtags)) {
                tweet += "\ncustom.hashtags=" + customHashtags;
            }
            if (StringUtils.isNotBlank(oneTimeSong)) {
                tweet +=  "\none.time.song=" + oneTimeSong;
            }
            if (StringUtils.isNotBlank(ignoredSong)) {
                tweet += "\nignored.song=" + ignoredSong;

            }
            googliTweeter.tweet(tweet);
            tweetedConfigs = true;
        }
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
            } else {
                LOG.warn("HFB found no tweets in given time period.");
            }
        }
        return cleanedSongName;
    }

    private boolean sameTweet(String fetchedSongName) {
        return fetchedSongName.equals(currentSongName);
    }

    private boolean shouldIgnoreTweet(String fetchedSongName)  {
        if (fetchedSongName == null) {
            LOG.warn("Skipping empty tweet");
            return true;

        }
        if (fetchedSongName.contains("\uD83D\uDCF8")
                || fetchedSongName.contains("@rene_huemer")
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

    private String cleanSongName(String fetchedSongName) {
        return fetchedSongName
                .replaceAll("&gt; ", "")
                .replaceAll("&gt;", "")
                .replaceAll("SET ONE: ", "")
                .replaceAll("SET TWO: ", "")
                .replaceAll("ENCORE: ", "");
    }
}