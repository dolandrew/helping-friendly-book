package helpingfriendlybook.service;

import helpingfriendlybook.config.HFBConfig;
import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.FriendshipDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import helpingfriendlybook.dto.TwitterUsersResponseDTO;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class TwitterService {
    private static final Logger LOG = LoggerFactory.getLogger(TwitterService.class);

    private final HFBConfig creds;

    private final Environment environment;

    private final RestTemplate restTemplate;

    public TwitterService(RestTemplate restTemplate, Environment environment, HFBConfig creds) {
        this.restTemplate = restTemplate;
        this.environment = environment;
        this.creds = creds;
    }

    public void favoriteTweetById(String id) {
        String url = "https://api.twitter.com/1.1/favorites/create.json?id=" + id;
        String successMessage = "Successfully liked tweet.";
        String failureMessage = "Error trying to like tweet.";

        post(url, successMessage, failureMessage, creds.getApiKey(), creds.getApiKeySecret(),
                creds.getAccessToken(), creds.getAccessTokenSecret(), null);
    }

    public Integer followFavoritesById(String tweetId) {
        String url = "https://api.twitter.com/2/tweets/" + tweetId + " /liking_users";
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), TwitterResponseDTO.class);
        if (response.getBody() != null && response.getBody().getData() != null) {
            response.getBody().getData().forEach(data -> followUser(data.getId()));
            return response.getBody().getData().size();
        }
        return 0;
    }

    public ResponseEntity<TwitterUsersResponseDTO> getFollowersList(String username) {
        LOG.warn("Getting followers for " + username + "...");
        String url = "https://api.twitter.com/1.1/followers/list.json?count=200&screen_name=" + username;
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), TwitterUsersResponseDTO.class);
    }

    public List<DataDTO> getFriendsList(String username) {
        LOG.warn("Getting friends for " + username + "...");
        Long cursor = -1L;
        List<DataDTO> users = new ArrayList<>();
        while (cursor != 0L) {
            String url = "https://api.twitter.com/1.1/friends/list.json?cursor=" + cursor + "&count=200&screen_name=" + username;
            TwitterUsersResponseDTO response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), TwitterUsersResponseDTO.class).getBody();
            users.addAll(response.getUsers());
            cursor = response.getNext_cursor();
        }
        LOG.warn("Found " + users.size() + " friends for " + username + "...");
        return users;
    }

    public ResponseEntity<TwitterResponseDTO> getTweetsForUserId(String userId) {
        LOG.warn("Checking for tweets...");
        String url = "https://api.twitter.com/2/users/" + userId + " /tweets?max_results=5";
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), TwitterResponseDTO.class);
    }

    public ResponseEntity<TwitterResponseDTO> getTweetsForUserIdInLastFiveMinutes(String userId) {
        LOG.warn("Checking for tweets...");
        var url = "https://api.twitter.com/2/users/" + userId + " /tweets?exclude=retweets,replies&max_results=5&start_time=" + getFiveMinutesAgo();
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), TwitterResponseDTO.class);
    }

    public ResponseEntity<FriendshipDTO> showFriendship(String screenName) {
        LOG.warn("Getting friendship between me and " + screenName + "...");
        String url = "https://api.twitter.com/1.1/friendships/show.json?source_screen_name=PhishCompanion&target_screen_name=" + screenName;
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), FriendshipDTO.class);
    }

    public ResponseEntity<DataDTO> showUser(String screenName) {
        LOG.warn("Getting " + screenName + "...");
        String url = "https://api.twitter.com/1.1/users/show.json?screen_name=" + screenName;
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), DataDTO.class);
    }

    public void tweet(String tweet, String apiKey, String apiKeySecret, String accessToken, String accessTokenSecret) {
        String encodedTweet = URLEncoder.encode(tweet, Charset.defaultCharset());
        String url = "https://api.twitter.com/1.1/statuses/update.json?status=" + encodedTweet;
        String failureMessage = "Error trying to tweet: \"" + tweet + "\".";

        post(url, null, failureMessage, apiKey, apiKeySecret, accessToken, accessTokenSecret, tweet);
    }

    public void tweet(String tweet) {
        if (tweet == null) {
            return;
        }
        String encodedTweet = URLEncoder.encode(tweet, Charset.defaultCharset());
        String url = "https://api.twitter.com/1.1/statuses/update.json?status=" + encodedTweet;
        String successMessage = "Tweeted: \"" + encodedTweet + "\".";
        String failureMessage = "Error trying to tweet: \"" + encodedTweet + "\".";

        post(url, successMessage, failureMessage, creds.getApiKey(), creds.getApiKeySecret(),
                creds.getAccessToken(), creds.getAccessTokenSecret(), tweet);
    }

    public void unfollow(DataDTO user) {
        LOG.warn("Unfollowing " + user.getScreenName() + "...");
        String url = "https://api.twitter.com/1.1/friendships/destroy.json?user_id=" + user.getId();
        String failureMessage = "Error trying to unfollow: \"" + user.getScreenName() + "\".";
        String successMessage = "Successfully unfollowed: \"" + user.getScreenName() + "\".";

        if (localEnvironment()) {
            LOG.warn("Would have unfollowed: " + user.getScreenName());
            return;
        }
        post(url, successMessage, failureMessage, creds.getApiKey(), creds.getApiKeySecret(), creds.getAccessToken(), creds.getAccessTokenSecret(), null);
    }

    private void followUser(String userId) {
        LOG.warn("Attempting to follow user with id: " + userId);
        String url = "https://api.twitter.com/1.1/friendships/create.json?user_id=" + userId;
        String successMessage = "Successfully followed user with id: " + userId;
        String failureMessage = "Error trying to follow user with id: " + userId;

        post(url, successMessage, failureMessage, creds.getApiKey(), creds.getApiKeySecret(),
                creds.getAccessToken(), creds.getAccessTokenSecret(), null);
    }

    private String getFiveMinutesAgo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss'Z'");
        return ZonedDateTime.now(ZoneId.of("UTC")).minus(1, MINUTES).format(formatter);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + creds.getBearerToken());
        return headers;
    }

    private boolean localEnvironment() {
        return environment.getActiveProfiles().length > 0 && environment.getActiveProfiles()[0].equals("local");
    }

    private void post(String url, String successMessage, String failureMessage, String apiKey, String apiKeySecret, String accessToken, String accessTokenSecret, String tweet) {
        try {
            OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(apiKey, apiKeySecret);
            oAuthConsumer.setTokenWithSecret(accessToken, accessTokenSecret);
            HttpPost httpPost = new HttpPost(url);
            oAuthConsumer.sign(httpPost);
            HttpClient httpClient = new DefaultHttpClient();
            if (localEnvironment()) {
                if (tweet != null) {
                    LOG.warn("Would have tweeted: " + tweet);
                }
            }
            httpClient.execute(httpPost);
            if (successMessage != null) {
                LOG.warn(successMessage);
            }
        } catch (Exception e) {
            LOG.error(failureMessage, e);
        }
    }
}