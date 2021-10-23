package helpingfriendlybook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogliTweeter {

    private static final Logger LOG = LoggerFactory.getLogger(GoogliTweeter.class);

    @Value("${twitter.googli.api.key}")
    private String apiKey;

    @Value("${twitter.googli.api.key.secret}")
    private String apiKeySecret;

    @Value("${twitter.googli.access.token}")
    private String accessToken;

    @Value("${twitter.googli.access.token.secret}")
    private String accessTokenSecret;

    private final TwitterService twitterService;

    public GoogliTweeter(TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    public void tweet(String tweet) {
        LOG.warn("@GoogliApparatus tweeted: \"" + tweet + "\"");
        twitterService.tweet(tweet + "\n\n" + System.currentTimeMillis(), apiKey, apiKeySecret, accessToken, accessTokenSecret);
    }

    public void tweet(String tweet, Throwable e) {
        LOG.warn("@GoogliApparatus tweeted: \"" + tweet + "\"", e);
        twitterService.tweet(tweet + ": " + e.getCause() + "\n\n" + System.currentTimeMillis(), apiKey, apiKeySecret, accessToken, accessTokenSecret);
    }
}
