package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public TweetListener(MetadataAssembler metadataAssembler, Tweeter tweeter, GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.metadataAssembler = metadataAssembler;
        this.tweeter = tweeter;
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
    }

    @Scheduled(initialDelay = 0, fixedDelay = 15000)
    public void listenToPhishFTR() {
        LOG.warn("Checking for tweets...");

        try {
            ResponseEntity<TwitterResponseDTO> responseEntity = twitterService.getTweets();
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                SongDTO songDTO = metadataAssembler.assembleMetadata(responseEntity);
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
}