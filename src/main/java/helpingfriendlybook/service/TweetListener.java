package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;

@EnableScheduling
@Service
public class TweetListener {

    private static final Logger LOG = LoggerFactory.getLogger(TweetListener.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MetadataAssembler metadataAssembler;

    private String currentSongName;

    @Scheduled(initialDelay = 0, fixedDelay = 5000)
    public void listenToPhishFTR() {
        LOG.info("Checking for tweets...");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer AAAAAAAAAAAAAAAAAAAAAN9tTQEAAAAAiPj%2FLykwYLZiH7LD8Hk0rwy8ptE%3DJufEgTBxTS6QqWeMMfXas1bVfn4oU24h2cRjyjiZsysPdyIVPj");
        Object response = restTemplate.exchange("https://api.twitter.com/2/users/1435725956371550213/tweets?max_results=5", HttpMethod.GET, new HttpEntity<>(headers), LinkedHashMap.class).getBody().get("data");

        String fetchedSongName = (String)((LinkedHashMap)((List) response).get(0)).get("text");
        if (fetchedSongName != null && (currentSongName == null || !fetchedSongName.equals(currentSongName))) {
            LOG.info("Found new song.");
            currentSongName = fetchedSongName;
            SongDTO songDTO = metadataAssembler.assembleMetadata(fetchedSongName);
            LOG.info(songDTO.getName());
            LOG.info(songDTO.getLastPlayed());
            LOG.info(songDTO.getGap().toString());
            LOG.info(songDTO.getLink());
        } else {
            LOG.info("Found no new songs.");
        }

    }
}