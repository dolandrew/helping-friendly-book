package helpingfriendlybook.service;

import helpingfriendlybook.dto.TwitterResponseDTO;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.Charset;

@Service
public class TwitterService {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterService.class);

    private final RestTemplate restTemplate;

    @Value("${twitter.bearerToken}")
    private String bearerToken;

    @Value("${twitter.phish.ftr.id}")
    private String phishFTRid;

    @Value("${twitter.api.key}")
    private String apiKey;

    @Value("${twitter.api.key.secret}")
    private String apiKeySecret;

    @Value("${twitter.access.token}")
    private String accessToken;

    @Value("${twitter.access.token.secret}")
    private String accessTokenSecret;

    private final Environment environment;

    public TwitterService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    public ResponseEntity<TwitterResponseDTO> getTweets() {
        LOG.warn("Checking for tweets...");
        String url = "https://api.twitter.com/2/users/" + phishFTRid + " /tweets?max_results=5";
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), TwitterResponseDTO.class);
    }

    public void tweet(String tweet, String apiKey, String apiKeySecret, String accessToken, String accessTokenSecret) {
        String encodedTweet = URLEncoder.encode(tweet, Charset.defaultCharset());
        String url = "https://api.twitter.com/1.1/statuses/update.json?status=" + encodedTweet;
        String failureMessage = "Error trying to tweet: \"" + tweet + "\".";

        post(url, null, failureMessage, apiKey, apiKeySecret, accessToken, accessTokenSecret);
    }

    public void tweet(String tweet) {
        if (tweet == null) {
            return;
        }
        String encodedTweet = URLEncoder.encode(tweet, Charset.defaultCharset());
        String url = "https://api.twitter.com/1.1/statuses/update.json?status=" + encodedTweet;
        String successMessage = "Tweeted: \"" + tweet + "\".";
        String failureMessage = "Error trying to tweet: \"" + tweet + "\".";

        post(url, successMessage, failureMessage, apiKey, apiKeySecret, accessToken, accessTokenSecret);
    }

    public void favoriteTweetById(String id) {
        String url = "https://api.twitter.com/1.1/favorites/create.json?id=" + id;
        String successMessage = "Successfully liked tweet.";
        String failureMessage = "Error trying to like tweet.";

        post(url, successMessage, failureMessage, apiKey, apiKeySecret, accessToken, accessTokenSecret);
    }

    private boolean localEnvironment() {
        return environment.getActiveProfiles().length > 0 && environment.getActiveProfiles()[0].equals("local");
    }

    private void followUser(String userId) {
        String url = "https://api.twitter.com/1.1/friendships/create.json?user_id=" + userId;
        String successMessage = "Successfully followed user with id: " + userId;
        String failureMessage = "Error trying to follow user with id: " + userId;

        post(url, successMessage, failureMessage, apiKey, apiKeySecret, accessToken, accessTokenSecret);
    }

    private void post(String url, String successMessage, String failureMessage, String apiKey, String apiKeySecret, String accessToken, String accessTokenSecret) {
        try {
            OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(apiKey, apiKeySecret);
            oAuthConsumer.setTokenWithSecret(accessToken, accessTokenSecret);
            HttpPost httpPost = new HttpPost(url);
            oAuthConsumer.sign(httpPost);
            HttpClient httpClient = new DefaultHttpClient();
            if (localEnvironment()) return;
            httpClient.execute(httpPost);
            if (successMessage != null) {
                LOG.warn(successMessage);
            }
        } catch (Exception e) {
            LOG.error(failureMessage, e);
        }
    }

    private void getFollowersList(String tweetId) {
        String url = "https://api.twitter.com/1.1/followers/list.json?screen/" + tweetId + " /liking_users";
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), TwitterResponseDTO.class);
        response.getBody().getData().forEach(data -> followUser(data.getId()));
    }

    private void followFavoritesById(String tweetId) {
        String url = "https://api.twitter.com/2/tweets/" + tweetId + " /liking_users";
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), TwitterResponseDTO.class);
        response.getBody().getData().forEach(data -> followUser(data.getId()));
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + bearerToken);
        return headers;
    }
}