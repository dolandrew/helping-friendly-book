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

    public void favoriteTweetById(String id) {
        String url = "https://api.twitter.com/1.1/favorites/create.json?id=" + id;

        OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(apiKey, apiKeySecret);
        oAuthConsumer.setTokenWithSecret(accessToken, accessTokenSecret);

        HttpPost httpPost = new HttpPost(url);

        try {
            oAuthConsumer.sign(httpPost);

            HttpClient httpClient = new DefaultHttpClient();
            if (environment.getActiveProfiles().length > 0 && environment.getActiveProfiles()[0].equals("local")) {
                return;
            }
            httpClient.execute(httpPost);
            LOG.warn("Successfully liked tweet.");

        } catch (Exception e) {
            LOG.error("Error trying to like tweet.");
        }
    }

    public ResponseEntity<TwitterResponseDTO> getTweets() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + bearerToken);
        String url = "https://api.twitter.com/2/users/" + phishFTRid + " /tweets?max_results=5";
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), TwitterResponseDTO.class);

    }
}