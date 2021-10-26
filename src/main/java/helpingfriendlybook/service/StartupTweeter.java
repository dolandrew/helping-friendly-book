package helpingfriendlybook.service;

import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StartupTweeter {
    private final GoogliTweeter googliTweeter;

    @Value("${one.time.song}")
    private String oneTimeSong;

    @Value("${bustout.threshold}")
    private Integer bustoutThreshold;

    @Value("${custom.hashtags}")
    private String customHashtags;

    @Value("${ignored.song}")
    private String ignoredSong;

    public StartupTweeter(GoogliTweeter googliTweeter) {
        this.googliTweeter = googliTweeter;
    }

    @PostConstruct
    public void tweetPropertiesOnStartup() {
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
    }
}