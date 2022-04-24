package helpingfriendlybook.service;

import helpingfriendlybook.config.GoogliConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GoogliTweeter {
    private static final Logger LOG = LoggerFactory.getLogger(GoogliTweeter.class);

    private final GoogliConfig creds;

    private final TwitterService twitterService;

    public GoogliTweeter(GoogliConfig creds, TwitterService twitterService) {
        this.creds = creds;
        this.twitterService = twitterService;
    }

    public void tweet(String tweet) {
        LOG.warn("@GoogliApparatus tweeted: \"" + tweet + "\"");
        twitterService.tweet(tweet + "\n\n" + System.currentTimeMillis(),
                creds.getApiKey(), creds.getApiKeySecret(),
                creds.getAccessToken(), creds.getAccessTokenSecret());
    }

    public void tweet(String tweet, Throwable e) {
        LOG.warn("@GoogliApparatus tweeted: \"" + tweet + "\"", e);
        twitterService.tweet(tweet + ": " + e.getCause() + "\n\n" + System.currentTimeMillis(),
                creds.getApiKey(), creds.getApiKeySecret(),
                creds.getAccessToken(), creds.getAccessTokenSecret());
    }
}
