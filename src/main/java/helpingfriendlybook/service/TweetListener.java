package helpingfriendlybook.service;

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

    private final Tweeter tweeter;

    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    @Value("${one.time.song}")
    private String oneTimeSong;

    @Value("${bustout.threshold}")
    private Integer bustoutThreshold;

    @Value("${custom.hashtags}")
    private String customHashtags;

    @Value("${cron}")
    private String cron;

    private boolean processed;

    public TweetListener(MetadataAssembler metadataAssembler, Tweeter tweeter, GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.metadataAssembler = metadataAssembler;
        this.tweeter = tweeter;
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
        googliTweeter.tweet("HFB started with properties\ncron=" + cron + "\nbustout.threshold=" + bustoutThreshold + "\ncustom.hashtags=" + customHashtags + "\none.time.song=" + oneTimeSong);
    }

    @Scheduled(cron="${cron}")
    public void listenToPhishFTR() {
        LOG.warn("Checking for tweets...");

        try {
            if (processed) {
                LOG.warn("Skipping because already processed one.time.song");
                return;
            }

            if (StringUtils.isNotBlank(oneTimeSong)) {
                processOneTimeTweet();
                return;
            }

            ResponseEntity<TwitterResponseDTO> responseEntity = twitterService.getTweets();
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                SongDTO songDTO = metadataAssembler.processTweet(responseEntity);
                if (songDTO != null) {
                    twitterService.favoriteTweetById(responseEntity.getBody().getData().get(0).getId());
                    tweeter.tweet(songDTO);
                }
            } else {
                LOG.error("Unable to fetch tweets.");
                googliTweeter.tweet("Error - HFB was unable to fetch tweets");
            }
        } catch (Exception e) {
            googliTweeter.tweet("HFB caught exception: " + e.getCause());
        }
    }

    private void processOneTimeTweet() {
        LOG.warn("Found one time song from config one.time.song");
        googliTweeter.tweet("Found one time song: " + oneTimeSong);
        SongDTO songDTO = metadataAssembler.assembleMetadata(oneTimeSong);
        tweeter.tweet(songDTO);

        oneTimeSong = null;
        processed = true;
    }
}