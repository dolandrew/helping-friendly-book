package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static java.lang.String.format;

@EnableScheduling
@Service
public class TweetListener {

    private static final Logger LOG = LoggerFactory.getLogger(TweetListener.class);

    private final RestTemplate restTemplate;

    private final MetadataAssembler metadataAssembler;

    private final Tweeter tweeter;

    private final GoogliTweeter googliTweeter;

    private String currentSongName;

    @Value("${twitter.bearerToken}")
    private String bearerToken;

    @Value("${twitter.phish.ftr.id}")
    private String phishFTRid;

    public TweetListener(RestTemplate restTemplate, MetadataAssembler metadataAssembler, Tweeter tweeter, GoogliTweeter googliTweeter) {
        this.restTemplate = restTemplate;
        this.metadataAssembler = metadataAssembler;
        this.tweeter = tweeter;
        this.googliTweeter = googliTweeter;
        this.googliTweeter.tweet(format("HelpingFriendlyBook started successfully at %s.", new Date()));
    }

    @Scheduled(initialDelay = 0, fixedDelay = 20000)
    public void listenToPhishFTR() {
        LOG.warn("Checking for tweets...");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + bearerToken);
        ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange("https://api.twitter.com/2/users/" + phishFTRid + " /tweets?max_results=5", HttpMethod.GET, new HttpEntity<>(headers), LinkedHashMap.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LinkedHashMap body = responseEntity.getBody();
            if (body != null) {
                Object data = body.get("data");
                if (data != null) {
                    String fetchedSongName = (String) ((LinkedHashMap) ((List) data).get(0)).get("text");
                    String cleanedSongName = cleanSongName(fetchedSongName);
                    if (sameTweet(fetchedSongName)) {
                        return;
                    }
                    currentSongName = cleanedSongName;
                    if (shouldIgnoreTweet(fetchedSongName)) {
                        return;
                    }
                    LOG.warn("Found new song: " + cleanedSongName);
                    SongDTO songDTO = metadataAssembler.assembleMetadata(cleanedSongName);
                    tweeter.tweet(songDTO);
                } else {
                    LOG.error("Found no tweets!");
                }
            }
        } else {
            LOG.error("Unable to fetch tweets.");
        }
    }

    private boolean sameTweet(String fetchedSongName) {
        if (fetchedSongName.equals(currentSongName)) {
            LOG.warn("No new tweets");
            return true;
        }
        return false;
    }

    private boolean shouldIgnoreTweet(String fetchedSongName)  {
        if (fetchedSongName == null) {
            LOG.warn("Skipping empty tweet");
            return true;

        }
        if (fetchedSongName.contains("\uD83D\uDCF8")
                || fetchedSongName.contains("@rene_huemer")
                || fetchedSongName.contains("https://t.co")
                || fetchedSongName.matches(".*[0-9]+/[0-9]+/[0-9]{4}.*")
        ) {
            LOG.warn("Skipping tweet: " + fetchedSongName);
            googliTweeter.tweet(format("Ignored @Phish_FTR tweet at %s", new Date()));
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