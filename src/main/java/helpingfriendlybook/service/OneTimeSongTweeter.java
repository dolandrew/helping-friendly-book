package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class OneTimeSongTweeter {

    private static final Logger LOG = LoggerFactory.getLogger(OneTimeSongTweeter.class);

    private final MetadataAssembler metadataAssembler;

    private final TweetWriter tweetWriter;

    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    private final Environment environment;

    @Value("${one.time.song}")
    private String oneTimeSong;

    @Value("${bustout.threshold}")
    private Integer bustoutThreshold;

    public OneTimeSongTweeter(MetadataAssembler metadataAssembler, TweetWriter tweetWriter, GoogliTweeter googliTweeter, TwitterService twitterService, Environment environment) {
        this.metadataAssembler = metadataAssembler;
        this.tweetWriter = tweetWriter;
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
        this.environment = environment;
    }

    @PostConstruct
    public void oneTimeSong() {
        if (StringUtils.isNotBlank(oneTimeSong)) {
            googliTweeter.tweet("Found one time song: " + oneTimeSong);
            SongDTO songDTO = metadataAssembler.assembleMetadata(oneTimeSong);
            String tweet = tweetWriter.writeTweet(songDTO, bustoutThreshold);
            if (localEnvironment()) {
                LOG.warn("Would have tweeted: " + tweet);
                return;
            }
            twitterService.tweet(tweet);
        }
    }

    private boolean localEnvironment() {
        return environment.getActiveProfiles().length > 0 && environment.getActiveProfiles()[0].equals("local");
    }
}