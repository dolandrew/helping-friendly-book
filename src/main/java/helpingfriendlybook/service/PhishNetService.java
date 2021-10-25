package helpingfriendlybook.service;

import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

@Service
public class PhishNetService {

    private final RestTemplate restTemplate;

    private final TwitterService twitterService;

    private final GoogliTweeter googliTweeter;

    private final TweetWriter tweetWriter;

    public PhishNetService(RestTemplate restTemplate,
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
            googliTweeter.tweet("Looking for shows on " + today.getMonthValue() + "-" + today.getDayOfMonth() + "...");
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
            googliTweeter.tweet("HFB found " + setlists + " shows on " + today.getMonthValue() + "-" + today.getDayOfMonth() + ".");
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
        tweet = tweetWriter.addShowHashtags(tweet);

        twitterService.tweet(tweet);
    }

    private void tweetRandomShow() {
        googliTweeter.tweet("Tweeting a random setlist...");
        String url = "https://phish.net/setlists/jump/random";
        String response = restTemplate.getForObject(url, String.class);
        processResponse(response, null);
    }

    private String addLink(Element element, String tweet) {
        String link = "phish.net" + element.getElementsByClass("setlist-date-long").get(0).getElementsByTag("a").get(1).attr("href");
        tweet += link;
        return tweet;
    }

    private String addLocation(Element element, String tweet) {
        String location = element.getElementsByClass("setlist-location").get(0).wholeText().replace("\n", "").replace("\t", "");
        tweet += location + " " + " \n";
        return tweet;
    }

    private String addVenue(Element element, String tweet) {
        String venue = element.getElementsByClass("setlist-venue").get(0).getElementsByTag("span").get(0).wholeText();
        tweet += WordUtils.capitalize(venue.toLowerCase(), ' ') + " ";
        return tweet;
    }

    private String addActualDate(Element element, String tweet) {
        String actualDate = element.getElementsByClass("setlist-date-long").get(0).getElementsByTag("a").get(1).wholeText();
        tweet += WordUtils.capitalize(actualDate.toLowerCase(), ' ') + "\n";
        return tweet;
    }
}