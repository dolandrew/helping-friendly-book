package helpingfriendlybook.service;

import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.SongDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

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
    }

    @Scheduled(initialDelay = 0, fixedDelay = 10000)
    public void listenToPhishFTR() {
        LOG.warn("Checking for tweets...");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + bearerToken);
        String url = "https://api.twitter.com/2/users/" + phishFTRid + " /tweets?max_results=5";
        var responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), TwitterResponseDTO.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            TwitterResponseDTO body = responseEntity.getBody();
            if (body != null) {
                if (body.getData() != null) {
                    DataDTO data = body.getData().get(0);
                    String fetchedSongName = data.getText();
                    String cleanedSongName = cleanSongName(fetchedSongName);
                    if (sameTweet(cleanedSongName)) {
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