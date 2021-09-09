package helpingfriendlybook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

@Service
public class Tweeter {

    private static final Logger LOG = LoggerFactory.getLogger(Tweeter.class);

    private final RestTemplate restTemplate;

    @Value("${twitter.bearerToken}")
    private String bearerToken;

    public Tweeter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void tweet() {
        LOG.info("Tweeting...");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + bearerToken);
        ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange("https://api.twitter.com/1.1/statuses/update.json?status=hello", HttpMethod.POST, new HttpEntity<>(headers), LinkedHashMap.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException();
        }
    }
}