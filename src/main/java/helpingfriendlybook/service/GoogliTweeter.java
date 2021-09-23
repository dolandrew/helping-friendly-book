package helpingfriendlybook.service;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.Charset;

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

    private final Environment environment;

    public GoogliTweeter(Environment environment) {
        this.environment = environment;
    }

    public void tweet(String tweet) {
        OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(apiKey, apiKeySecret);
        oAuthConsumer.setTokenWithSecret(accessToken, accessTokenSecret);

        String encodedTweet = URLEncoder.encode(tweet, Charset.defaultCharset());

        HttpPost httpPost = new HttpPost(
                "https://api.twitter.com/1.1/statuses/update.json?status=" + encodedTweet);

        try {
            oAuthConsumer.sign(httpPost);
            LOG.warn("Created tweet: \"" + tweet + "\"");

            HttpClient httpClient = new DefaultHttpClient();
            if (environment.getActiveProfiles().length > 0  && environment.getActiveProfiles()[0].equals("local")) {
                return;
            }
            LOG.warn("Tweeting...");
            httpClient.execute(httpPost);
            LOG.warn("Successfully posted tweet.");
        } catch (Exception e) {
            LOG.error("Error trying to tweet: \"" + tweet + "\"");
            return;
        }
    }
}
