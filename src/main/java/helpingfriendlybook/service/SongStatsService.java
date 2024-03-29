package helpingfriendlybook.service;

import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.SongDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

import static java.lang.Integer.parseInt;

@EnableScheduling
@Service
public final class SongStatsService {
    private static final Logger LOG = LoggerFactory.getLogger(SongStatsService.class);

    private final GoogliTweeter googliTweeter;

    private final MetadataAssembler metadataAssembler;

    private final TimeApiService timeApiService;

    private final TweetWriter tweetWriter;

    private final TwitterService twitterService;

    private final PhishDotNetProxyService phishDotNetProxyService;

    @Value("${bustout.threshold}")
    private Integer bustoutThreshold;

    private String currentSongName;

    @Value("${ignored.song}")
    private String ignoredSong;

    @Value("${twitter.phish.ftr.id}")
    private String phishFTRid;

    @Value("${show.timezone.abbr}")
    private String showTimezoneAbbr;

    public SongStatsService(final MetadataAssembler assembler, final TweetWriter tw, final GoogliTweeter googli,
                            final TwitterService ts, final TimeApiService tApiService, final PhishDotNetProxyService phishDotNetProxyService) {
        this.metadataAssembler = assembler;
        this.tweetWriter = tw;
        this.googliTweeter = googli;
        this.twitterService = ts;
        this.timeApiService = tApiService;
        this.phishDotNetProxyService = phishDotNetProxyService;
    }

    public static String cleanSongName(final String fetchedSongName) {
        return fetchedSongName
                .replaceAll("&gt; ", "")
                .replaceAll("&gt;", "")
                .replaceAll("> ", "")
                .replaceAll(">", "")
                .replaceAll("SET ONE: ", "")
                .replaceAll("SET TWO: ", "")
                .replaceAll("SET THREE: ", "")
                .replaceAll("SET FOUR: ", "")
                .replaceAll("SET FIVE: ", "")
                .replaceAll("ENCORE TWO: ", "")
                .replaceAll("ENCORE: ", "");
    }

    @Scheduled(cron = "${cron.listen}")
    public void listenToPhishDotNet() {
        try {
            final LocalDate today = LocalDate.now(ZoneId.of("UTC-8"));
            String songName = phishDotNetProxyService.getLastPlayedSongForDate(today.getDayOfMonth(), today.getMonthValue(), today.getYear() );
            if (songName != null) {
                if (songName.equals(ignoredSong)) {
                    googliTweeter.tweet("Ignored expected song: " + ignoredSong);
                } else {
                    processOutgoingTweet(songName);
                }
            }
        } catch (Exception e) {
            googliTweeter.tweet("HFB exception while listening: ", e);
        }
    }

    public void checkForSetStart(final String tweet) {
        if (tweet.contains("SET ONE:") || tweet.toLowerCase(Locale.ROOT).contains("set one:")) {
            tweetSetStart("SET ONE", "\uD83C\uDF89");
        } else if (tweet.contains("SET TWO:") || tweet.toLowerCase(Locale.ROOT).contains("set two:")) {
            tweetSetStart("SET TWO", "\uD83D\uDC20");
        } else if (tweet.contains("SET THREE:") || tweet.toLowerCase(Locale.ROOT).contains("set three:")) {
            tweetSetStart("SET THREE", "\uD83D\uDD7A");
        } else if (tweet.contains("ENCORE:") || tweet.toLowerCase(Locale.ROOT).contains("encore:")) {
            tweetSetStart("ENCORE", "⭕️");
        }
    }

    private String processIncomingTweet(final ResponseEntity<TwitterResponseDTO> responseEntity) {
        String cleanedSongName = null;
        TwitterResponseDTO body = responseEntity.getBody();
        if (body != null) {
            if (body.getData() != null) {
                DataDTO data = body.getData().get(0);
                String fetchedSongName = data.getText();
                String tweetId = data.getId();
                twitterService.favoriteTweetById(tweetId);
                if (shouldIgnoreTweet(fetchedSongName)) {
                    return null;
                }
                cleanedSongName = cleanSongName(fetchedSongName);
                if (sameTweet(cleanedSongName)) {
                    LOG.info("Found no new tweets.");
                    return null;
                }
                LOG.info("Found new tweet.");
                currentSongName = cleanedSongName;
                checkForSetStart(fetchedSongName);
            } else {
                LOG.info("HFB found no tweets in given time period.");
            }
        }
        return cleanedSongName;
    }

    private void processOutgoingTweet(final String songName) {
        SongDTO songDTO = metadataAssembler.assembleMetadata(songName);
        String tweet = tweetWriter.writeSongStatsTweet(songDTO, bustoutThreshold);
        twitterService.tweet(tweet);
    }

    private boolean sameTweet(final String fetchedSongName) {
        return fetchedSongName.equals(currentSongName);
    }

    private boolean shouldIgnoreTweet(final String fetchedSongName) {
        if (fetchedSongName == null) {
            LOG.warn("Skipping empty tweet");
            return true;
        }
        if (fetchedSongName.contains("\uD83D\uDCF8")
                || fetchedSongName.contains("@rene_huemer")
                || fetchedSongName.contains(">>")
                || fetchedSongName.contains("https://t.co")
                || fetchedSongName.matches(".*[0-9]+/[0-9]+/[0-9]{4}.*")
        ) {
            LOG.warn("Skipping tweet: " + fetchedSongName);
            return true;
        }
        if (fetchedSongName.startsWith("RT ")) {
            LOG.warn("Skipping retweet: " + fetchedSongName);
            return true;
        }
        return false;
    }

    private void tweetSetStart(final String setName, final String emoji) {
        String timeInLosAngeles = timeApiService.getTimeAtShow();
        String[] timeParts = timeInLosAngeles.split(":");
        int hour = parseInt(timeParts[0]) % 12;
        if (hour == 0) {
            hour = 12;
        }
        twitterService.tweet(emoji + " " + setName + " started at " + hour + ":" + timeParts[1] + (parseInt(timeParts[0]) >= 12 ? " PM " : " AM ") + showTimezoneAbbr);
    }
}
