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

    @Value("${twitter.phish.companion.id}")
    private String phishCompanionId;

    public TweetListener(RestTemplate restTemplate, MetadataAssembler metadataAssembler, Tweeter tweeter) {
        this.restTemplate = restTemplate;
        this.metadataAssembler = metadataAssembler;
        this.tweeter = tweeter;
    }

    @Scheduled(initialDelay = 0, fixedDelay = 60000 * 20)
    public void listenToPhishFTR() throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException {
        LOG.info("Checking for tweets...");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + bearerToken);
        ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange("https://api.twitter.com/2/users/" + phishCompanionId + " /tweets?max_results=5", HttpMethod.GET, new HttpEntity<>(headers), LinkedHashMap.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LinkedHashMap body = responseEntity.getBody();
            if (body != null) {
                Object data = body.get("data");
                if (data != null) {
                    String fetchedSongName = (String) ((LinkedHashMap) ((List) data).get(0)).get("text");
                    if (fetchedSongName != null && (currentSongName == null || !fetchedSongName.equals(currentSongName))) {
                        LOG.info("Found new song.");
                        currentSongName = fetchedSongName;
                        SongDTO songDTO = metadataAssembler.assembleMetadata(fetchedSongName);
                        tweeter.tweet(songDTO);
                        LOG.info(songDTO.getName());
                        LOG.info(songDTO.getTimes().toString());
                        LOG.info(songDTO.getDebut());
                        LOG.info(songDTO.getLastPlayed());
                        LOG.info(songDTO.getGap().toString());
                        LOG.info(songDTO.getLink());
                    } else {
                        LOG.info("Found no new songs.");
                    }
                } else {
                    LOG.info("Found no tweets.");
                }
            }
        } else {
            LOG.error("Unable to fetch tweets.");
            throw new RuntimeException();
        }
    }
}