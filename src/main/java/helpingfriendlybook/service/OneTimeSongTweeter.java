package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class OneTimeSongTweeter {

    private final MetadataAssembler metadataAssembler;

    private final TweetWriter tweetWriter;

    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    @Value("${one.time.song}")
    private String oneTimeSong;

    @Value("${bustout.threshold}")
    private Integer bustoutThreshold;

    public OneTimeSongTweeter(MetadataAssembler metadataAssembler, TweetWriter tweetWriter, GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.metadataAssembler = metadataAssembler;
        this.tweetWriter = tweetWriter;
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
    }

    @PostConstruct
    public void oneTimeSong() {
        if (StringUtils.isNotBlank(oneTimeSong)) {
            googliTweeter.tweet("Found one time song: " + oneTimeSong);
            SongDTO songDTO = metadataAssembler.assembleMetadata(oneTimeSong);
            String tweet = tweetWriter.writeTweet(songDTO, bustoutThreshold);
            twitterService.tweet(tweet);
        }
    }
}