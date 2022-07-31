package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static helpingfriendlybook.service.SongStatsService.cleanSongName;

@Service
public class OneTimeSongTweeter {
    private static final Logger LOG = LoggerFactory.getLogger(OneTimeSongTweeter.class);

    private final Environment environment;

    private final GoogliTweeter googliTweeter;

    private final MetadataAssembler metadataAssembler;

    private final TweetWriter tweetWriter;

    private final TwitterService twitterService;

    private final SongStatsService songStatsService;

    @Value("${bustout.threshold}")
    private Integer bustoutThreshold;

    @Value("${one.time.song}")
    private String oneTimeSong;

    public OneTimeSongTweeter(MetadataAssembler metadataAssembler, TweetWriter tweetWriter, GoogliTweeter googliTweeter, TwitterService twitterService, Environment environment, SongStatsService songStatsService) {
        this.metadataAssembler = metadataAssembler;
        this.tweetWriter = tweetWriter;
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
        this.environment = environment;
        this.songStatsService = songStatsService;
    }

    @PostConstruct
    public void oneTimeSong() {
        if (StringUtils.isNotBlank(oneTimeSong)) {
            googliTweeter.tweet("Found one time song: " + oneTimeSong);
            songStatsService.checkForSetStart(oneTimeSong);
            String oneTimeSongCleaned = cleanSongName(oneTimeSong);
            SongDTO songDTO = metadataAssembler.assembleMetadata(oneTimeSongCleaned);
            String tweet = tweetWriter.writeSongStatsTweet(songDTO, bustoutThreshold);
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