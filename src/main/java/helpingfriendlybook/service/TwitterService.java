package helpingfriendlybook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import helpingfriendlybook.config.HFBConfig;
import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.TweetResponseDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import helpingfriendlybook.dto.TwitterUserResponseDTO;
import helpingfriendlybook.dto.TwitterUsersResponseDTO;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

@Service
public class TwitterService {
    private static final Logger LOG = LoggerFactory.getLogger(TwitterService.class);

    private final HFBConfig creds;

    private final Environment environment;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public TwitterService(RestTemplate restTemplate, Environment environment, HFBConfig creds, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.environment = environment;
        this.creds = creds;
        this.objectMapper = objectMapper;
    }

    public static String getOneMinuteAgo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss'Z'");
        return ZonedDateTime.now(ZoneId.of("UTC")).minus(1, MINUTES).format(formatter);
    }

    public static String getThirtySecondsAgo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss'Z'");
        return ZonedDateTime.now(ZoneId.of("UTC")).minus(30, SECONDS).format(formatter);
    }

    public static String getSomeHoursAgo(int intervalHours) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss'Z'");
        return ZonedDateTime.now(ZoneId.of("UTC")).minus(intervalHours, HOURS).format(formatter);
    }

    public void favoriteTweetById(String id) {
        String url = "https://api.twitter.com/1.1/favorites/create.json?id=" + id;
        String successMessage = "Successfully liked tweet.";
        String failureMessage = "Error trying to like tweet.";

        post(url, successMessage, failureMessage, creds.getApiKey(), creds.getApiKeySecret(), creds.getAccessToken(), creds.getAccessTokenSecret(), null);
    }

    public Integer followFavoritesById(String tweetId) {
        String url = "https://api.twitter.com/2/tweets/" + tweetId + " /liking_users";
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeadersWithBearerToken()), TwitterResponseDTO.class);
        if (response.getBody() != null && response.getBody().getData() != null) {
            response.getBody().getData().forEach(data -> followUser(data.getId()));
            return response.getBody().getData().size();
        }
        return 0;
    }

    public List<DataDTO> getFollowersList(String username) {
        LOG.info("Getting followers for " + username + "...");
        Long cursor = -1L;
        List<DataDTO> users = new ArrayList<>();
        while (cursor != 0L) {
            String url = "https://api.twitter.com/1.1/followers/list.json?cursor=" + cursor + "&count=200&screen_name=" + username;
            TwitterUsersResponseDTO response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeadersWithBearerToken()), TwitterUsersResponseDTO.class).getBody();
            users.addAll(response.getUsers());
            cursor = response.getNext_cursor();
        }
        LOG.info("Found " + users.size() + " followers for " + username + ".");
        return users;
    }

    public List<DataDTO> getFriendsList(String username) {
        LOG.info("Getting friends for " + username + "...");
        Long cursor = -1L;
        List<DataDTO> users = new ArrayList<>();
        while (cursor != 0L) {
            String url = "https://api.twitter.com/1.1/friends/list.json?cursor=" + cursor + "&count=200&screen_name=" + username;
            TwitterUsersResponseDTO response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeadersWithBearerToken()), TwitterUsersResponseDTO.class).getBody();
            users.addAll(response.getUsers());
            cursor = response.getNext_cursor();
        }
        LOG.info("Found " + users.size() + " friends for " + username + ".");
        return users;
    }

    public TwitterUserResponseDTO getUserById(String userId) {
        LOG.info("Getting user " + userId + " by id...");
        String url = "https://api.twitter.com/2/users/" + userId;
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeadersWithBearerToken()), TwitterUserResponseDTO.class).getBody();
    }

    public ResponseEntity<TwitterResponseDTO> getTweetsAndRetweetsForUserIdInLast(String userId, String timeframe) {
        LOG.info("Checking for tweets...");
        var url = "https://api.twitter.com/2/users/" + userId + " /tweets?exclude=replies&max_results=5&start_time=" + timeframe;
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeadersWithBearerToken()), TwitterResponseDTO.class);
    }

    public ResponseEntity<TwitterResponseDTO> getTweetsForUserId(String userId) {
        LOG.info("Checking for tweets...");
        String url = "https://api.twitter.com/2/users/" + userId + " /tweets?max_results=5";
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeadersWithBearerToken()), TwitterResponseDTO.class);
    }

    public ResponseEntity<TwitterResponseDTO> getTweetsForUserIdInLast(String userId, String timeframe) {
        LOG.info("Checking for tweets...");
        var url = "https://api.twitter.com/2/users/" + userId + " /tweets?exclude=retweets,replies&max_results=5&start_time=" + timeframe;
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeadersWithBearerToken()), TwitterResponseDTO.class);
    }

    public ResponseEntity<TwitterResponseDTO> getMentionsForUserIdInLast(String userId, String timeframe) {
        LOG.info("Getting mentions for user " + userId + "...");
        var url = "https://api.twitter.com/2/users/" + userId + " /mentions?expansions=author_id&user.fields=username&start_time=" + timeframe;
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeadersWithBearerToken()), TwitterResponseDTO.class);
    }

    public ResponseEntity<DataDTO> showUser(String screenName) {
        LOG.info("Getting " + screenName + "...");
        String url = "https://api.twitter.com/1.1/users/show.json?screen_name=" + screenName;
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeadersWithBearerToken()), DataDTO.class);
    }

    public void tweet(String tweet, String apiKey, String apiKeySecret, String accessToken, String accessTokenSecret) {
        String encodedTweet = URLEncoder.encode(tweet, Charset.defaultCharset());
        String url = "https://api.twitter.com/1.1/statuses/update.json?status=" + encodedTweet;
        String failureMessage = "Error trying to tweet: \"" + tweet + "\".";

        post(url, null, failureMessage, apiKey, apiKeySecret, accessToken, accessTokenSecret, tweet);
    }

    public TweetResponseDTO tweet(String tweet, String inReplyTo) {
        if (tweet == null) {
            return null;
        }
        if (tweet.length() > 280) {
            String restOfTweet = tweet.substring(280);
            String firstPart = tweet.substring(0, 280);
            inReplyTo = tweet(firstPart, inReplyTo).getId();
            return tweet(restOfTweet, inReplyTo);
        } else {

            String encodedTweet = URLEncoder.encode(tweet, Charset.defaultCharset());
            String url = "https://api.twitter.com/1.1/statuses/update.json?status=" + encodedTweet;
            if (inReplyTo != null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                url += "&in_reply_to_status_id=" + inReplyTo;
            }
            String successMessage = "Tweeted: \"" + encodedTweet + "\".";
            String failureMessage = "Error trying to tweet: \"" + encodedTweet + "\".";

            return post(url, successMessage, failureMessage, creds.getApiKey(), creds.getApiKeySecret(),
                    creds.getAccessToken(), creds.getAccessTokenSecret(), tweet);
        }
    }

    public TweetResponseDTO tweet(String tweet) {
        return tweet(tweet, null);
    }

    public void unfollow(DataDTO user) {
        LOG.info("Unfollowing " + user.getScreenName() + "...");
        String url = "https://api.twitter.com/1.1/friendships/destroy.json?user_id=" + user.getId();
        String failureMessage = "Error trying to unfollow: \"" + user.getScreenName() + "\".";
        String successMessage = "Successfully unfollowed: \"" + user.getScreenName() + "\".";

        if (isLocalEnvironment()) {
            LOG.warn("Would have unfollowed: " + user.getScreenName());
            return;
        }
        post(url, successMessage, failureMessage, creds.getApiKey(), creds.getApiKeySecret(), creds.getAccessToken(), creds.getAccessTokenSecret(), null);
    }

    private void followUser(String userId) {
        LOG.info("Attempting to follow user with id: " + userId);
        String url = "https://api.twitter.com/1.1/friendships/create.json?user_id=" + userId;
        String successMessage = "Successfully followed user with id: " + userId;
        String failureMessage = "Error trying to follow user with id: " + userId;

        post(url, successMessage, failureMessage, creds.getApiKey(), creds.getApiKeySecret(), creds.getAccessToken(), creds.getAccessTokenSecret(), null);
    }

    private HttpHeaders getHeadersWithBearerToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + creds.getBearerToken());
        return headers;
    }

    private boolean isLocalEnvironment() {
        return environment.getActiveProfiles().length > 0 && environment.getActiveProfiles()[0].equals("local");
    }

    private TweetResponseDTO post(String url, String successMessage, String failureMessage, String apiKey, String apiKeySecret, String accessToken, String accessTokenSecret, String tweet) {
        try {
            OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(apiKey, apiKeySecret);
            oAuthConsumer.setTokenWithSecret(accessToken, accessTokenSecret);
            HttpPost httpPost = new HttpPost(url);
            oAuthConsumer.sign(httpPost);
            HttpClient httpClient = new DefaultHttpClient();
            if (isLocalEnvironment()) {
                if (tweet != null) {
                    LOG.warn("Would have tweeted: " + tweet);
                }
            } else {
                HttpResponse response = httpClient.execute(httpPost);
                return objectMapper.readValue(EntityUtils.toString(response.getEntity()), TweetResponseDTO.class);
            }
            if (successMessage != null) {
                LOG.warn(successMessage);
            }
        } catch (Exception e) {
            LOG.error(failureMessage, e);
        }
        return null;
    }
}