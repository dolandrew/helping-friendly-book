package helpingfriendlybook.service;

import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

@Service
public class OnThisDayService {

    private final RestTemplate restTemplate;

    private final TwitterService twitterService;

    private final GoogliTweeter googliTweeter;

    private final TweetWriter tweetWriter;

    private static final Logger LOG = LoggerFactory.getLogger(OnThisDayService.class);

    public OnThisDayService(RestTemplate restTemplate,
                            TwitterService twitterService,
                            GoogliTweeter googliTweeter,
                            TweetWriter tweetWriter) {
        this.restTemplate = restTemplate;
        this.twitterService = twitterService;
        this.googliTweeter = googliTweeter;
        this.tweetWriter = tweetWriter;
    }

    @Scheduled(cron="${cron.shows}")
    public void getShowsOnThisDay() {
        try {
            ZonedDateTime today = OffsetDateTime.now().atZoneSameInstant(ZoneId.of("America/Los_Angeles"));
            LOG.warn("Looking for shows on " + today.getMonthValue() + "-" + today.getDayOfMonth() + "...");
            String url = "https://phish.net/setlists/?month=" + today.getMonthValue() + "&day=" + today.getDayOfMonth();
            String response = restTemplate.getForObject(url, String.class);
            processResponse(response, today);
        } catch (Exception e) {
            googliTweeter.tweet("Caught exception trying to post shows on this day!" , e.getCause());
        }
    }

    private void processResponse(String response, ZonedDateTime today) {
        Document doc = Jsoup.parse(response);
        int setlists = doc.getElementsByClass("setlist").size();
        if (today != null) {
            LOG.warn("HFB found " + setlists + " shows on " + today.getMonthValue() + "-" + today.getDayOfMonth() + ".");
        }
        if (setlists == 0) {
            tweetRandomShow();
            return;
        }

        int random = new Random().nextInt(setlists);

        Element element = doc.getElementsByClass("setlist").get(random);
        String tweet = "";
        if (today != null) {
            tweet += "#OnThisDay\n";
        } else {
            tweet += "#RandomShow\n";
        }
        tweet = addActualDate(element, tweet);
        tweet = addVenue(element, tweet);
        tweet = addLocation(element, tweet);
        tweet = addLink(element, tweet);
        // TODO: add link to relisten, phish'n, phishtracks
        if (today != null) {
            if (setlists > 1) {
                tweet = addTotalShows(setlists, today, tweet);
            }
            tweet = tweetWriter.addShowHashtags(tweet);
        } else {
            tweet = tweetWriter.addRandomShowHashtags(tweet);

        }

        twitterService.tweet(tweet);
    }

    private String addActualDate(Element element, String tweet) {
        String actualDate = element.getElementsByClass("setlist-date-long").get(0).getElementsByTag("a").get(1).wholeText();
        tweet += WordUtils.capitalize(actualDate.toLowerCase(), ' ') + "\n";
        return tweet;
    }

    private String addVenue(Element element, String tweet) {
        String venue = element.getElementsByClass("setlist-venue").get(0).getElementsByTag("span").get(0).wholeText();
        tweet += WordUtils.capitalize(venue.toLowerCase(), ' ') + " ";
        return tweet;
    }

    private String addLocation(Element element, String tweet) {
        String location = element.getElementsByClass("setlist-location").get(0).wholeText().replace("\n", "").replace("\t", "");
        tweet += location.replaceAll(",", ", ") + " " + " \n";
        return tweet;
    }

    private String addLink(Element element, String tweet) {
        String link = "phish.net" + element.getElementsByClass("setlist-date-long").get(0).getElementsByTag("a").get(1).attr("href");
        tweet += link + "\n\n";
        return tweet;
    }

    private String addTotalShows(int totalShows, ZonedDateTime today, String tweet) {
        tweet += totalShows - 1 + " other shows on this date\n";
        tweet += "https://phish.net/setlists/?month=" + today.getMonthValue() + "&day=" + today.getDayOfMonth() + "\n";
        return tweet;
    }

    private void tweetRandomShow() {
        LOG.warn("Found no shows on this day. Tweeting a random setlist...");
        String url = "https://phish.net/setlists/jump/random";
        String response = restTemplate.getForObject(url, String.class);
        processResponse(response, null);
    }
}