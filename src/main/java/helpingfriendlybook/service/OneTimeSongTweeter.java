package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static helpingfriendlybook.service.TweetListener.cleanSongName;

@Service
public class OneTimeSongTweeter {
    private static final Logger LOG = LoggerFactory.getLogger(OneTimeSongTweeter.class);

    private final Environment environment;

    private final GoogliTweeter googliTweeter;

    private final MetadataAssembler metadataAssembler;

    private final TweetWriter tweetWriter;

    private final TwitterService twitterService;

    private final TweetListener tweetListener;

    @Value("${bustout.threshold}")
    private Integer bustoutThreshold;

    @Value("${one.time.song}")
    private String oneTimeSong;

    public OneTimeSongTweeter(MetadataAssembler metadataAssembler, TweetWriter tweetWriter, GoogliTweeter googliTweeter, TwitterService twitterService, Environment environment, TweetListener tweetListener) {
        this.metadataAssembler = metadataAssembler;
        this.tweetWriter = tweetWriter;
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
        this.environment = environment;
        this.tweetListener = tweetListener;
    }

    @PostConstruct
    public void oneTimeSong() {
        if (StringUtils.isNotBlank(oneTimeSong)) {
            googliTweeter.tweet("Found one time song: " + oneTimeSong);
            try {
                tweetListener.checkForSetStart(oneTimeSong);
            } catch (Exception e) {
                googliTweeter.tweet("Failed to tweet set start!");
            }
            String oneTimeSongCleaned = cleanSongName(oneTimeSong);
            SongDTO songDTO = metadataAssembler.assembleMetadata(oneTimeSongCleaned);
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