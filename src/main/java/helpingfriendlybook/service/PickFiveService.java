package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class PickFiveService {
    private static final Logger LOG = LoggerFactory.getLogger(PickFiveService.class);

    private final SongLoader songLoader;

    private final TwitterService twitterService;

    public PickFiveService(SongLoader songLoader, TwitterService twitterService) {
        this.songLoader = songLoader;
        this.twitterService = twitterService;
    }

    @Scheduled(cron = "${cron.pick.five}")
    public void pickFive() {
        LOG.info("Picking five...");
        List<SongDTO> songs = songLoader.getSongs();
        songs.sort(Comparator.comparing(SongDTO::getTimes).reversed());
        List<SongDTO> picks = songs.stream()
                .filter(song -> song.getGap() > 5)
                .filter(song -> song.getGap() < 20)
                .limit(75)
                .collect(toList());

        Collections.shuffle(picks);

        picks = picks.stream()
                .limit(5)
                .collect(toList());

        String tweet = "";
        for (SongDTO pick : picks) {
            tweet += pick.getName() + "\n";
        }
        tweet += "#phish #pick5";

        twitterService.tweet(tweet);
    }
}