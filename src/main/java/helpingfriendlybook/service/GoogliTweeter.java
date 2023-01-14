package helpingfriendlybook.service;

import helpingfriendlybook.config.GoogliConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GoogliTweeter {
    private static final Logger LOG = LoggerFactory.getLogger(GoogliTweeter.class);

    private final GoogliConfig googliConfig;

    private final TwitterService twitterService;

    public GoogliTweeter(final GoogliConfig config, final TwitterService ts) {
        this.googliConfig = config;
        this.twitterService = ts;
    }

    public void tweet(final String tweet) {
        LOG.warn("@GoogliApparatus tweeted: \"" + tweet + "\"");
        twitterService.tweet(tweet + "\n\n" + System.currentTimeMillis(),
                googliConfig.getApiKey(), googliConfig.getApiKeySecret(),
                googliConfig.getAccessToken(), googliConfig.getAccessTokenSecret());
    }

    public void tweet(final String tweet, final Throwable e) {
        LOG.error("@GoogliApparatus tweeted: \"" + tweet + "\"", e);

        String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();

        twitterService.tweet(tweet + ": " + message + "\n\n" + System.currentTimeMillis(),
                googliConfig.getApiKey(), googliConfig.getApiKeySecret(),
                googliConfig.getAccessToken(), googliConfig.getAccessTokenSecret());
    }
}
