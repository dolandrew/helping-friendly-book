package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

@EnableScheduling
@Service
public class TweetListener {

    private static final Logger LOG = LoggerFactory.getLogger(TweetListener.class);

    private final RestTemplate restTemplate;

    private final MetadataAssembler metadataAssembler;

    private final Tweeter tweeter;

    private String currentSongName;

    @Value("${twitter.bearerToken}")
    private String bearerToken;

    @Value("${twitter.phish.ftr.id}")
    private String phishFTRid;

    public TweetListener(RestTemplate restTemplate, MetadataAssembler metadataAssembler, Tweeter tweeter) {
        this.restTemplate = restTemplate;
        this.metadataAssembler = metadataAssembler;
        this.tweeter = tweeter;
    }

    @Scheduled(initialDelay = 0, fixedDelay = 60000 * 1)
    public void listenToPhishFTR() throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException {
        LOG.warn("Checking for tweets...");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + bearerToken);
        ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange("https://api.twitter.com/2/users/" + phishFTRid + " /tweets?max_results=1", HttpMethod.GET, new HttpEntity<>(headers), LinkedHashMap.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LinkedHashMap body = responseEntity.getBody();
            if (body != null) {
                Object data = body.get("data");
                if (data != null) {
                    String fetchedSongName = (String) ((LinkedHashMap) ((List) data).get(0)).get("text");
                    if (shouldIgnoreTweet(fetchedSongName)) {
                        return;
                    }
                    if (!shouldIgnoreTweet(fetchedSongName) && !fetchedSongName.equals(currentSongName)) {
                        String cleanedSongName = cleanSongName(fetchedSongName);
                        LOG.warn("Found new song: " + cleanedSongName);
                        currentSongName = cleanedSongName;
                        SongDTO songDTO = metadataAssembler.assembleMetadata(cleanedSongName);
                        tweeter.tweet(songDTO);
                    } else {
                        LOG.warn("Found no new songs.");
                    }
                } else {
                    LOG.error("Found no tweets!");
                }
            }
        } else {
            LOG.error("Unable to fetch tweets.");
        }
    }

    private boolean shouldIgnoreTweet(String fetchedSongName) {
        if (fetchedSongName == null
                ||fetchedSongName.contains("\uD83D\uDCF8")
                || fetchedSongName.contains("@rene_huemer")
                || fetchedSongName.contains("https://t.co")
                || fetchedSongName.matches(".*[0-9]+/[0-9]+/[0-9]{4}.*")
        ) {
            LOG.warn("Skipping tweet: " + fetchedSongName);
            return true;
        }
        return false;
    }

    private String cleanSongName(String fetchedSongName) {
        return fetchedSongName
                .replaceAll("&gt; ", "")
                .replaceAll("&gt;&gt; ", "")
                .replaceAll("SET ONE: ", "")
                .replaceAll("SET TWO: ", "")
                .replaceAll("ENCORE: ", "");
    }
}