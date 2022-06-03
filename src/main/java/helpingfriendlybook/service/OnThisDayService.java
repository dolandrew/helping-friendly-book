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
import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;

@Service
public class OnThisDayService {
    private static final Logger LOG = LoggerFactory.getLogger(OnThisDayService.class);

    private final GoogliTweeter googliTweeter;

    private final RestTemplate restTemplate;

    private final TweetWriter tweetWriter;

    private final TwitterService twitterService;

    public OnThisDayService(RestTemplate restTemplate,
                            TwitterService twitterService,
                            GoogliTweeter googliTweeter,
                            TweetWriter tweetWriter) {
        this.restTemplate = restTemplate;
        this.twitterService = twitterService;
        this.googliTweeter = googliTweeter;
        this.tweetWriter = tweetWriter;
    }

    public Element getRandomShowForDate(Integer day, Integer month, Integer year) {
        LOG.warn("Looking for shows on " + month + "-" + day + "...");
        String url = "https://phish.net/setlists/?month=" + month + "&day=" + day + (year != null ? "&year=" + year : "");
        String response = restTemplate.getForObject(url, String.class);
        List<Element> shows = getShowsFromResponse(response);
        if (shows.isEmpty()) {
            return null;
        }
        LOG.warn("HFB found " + shows.size() + " shows on " + (year != null ? year + "-" : "") + month + "-" + day + ".");
        int random = new Random().nextInt(shows.size());
        return shows.get(random);
    }

    public String getVenueOfShow(Element element) {
        String venueRaw = element.getElementsByClass("setlist-venue").get(0).getElementsByTag("span").get(0).wholeText();
        return WordUtils.capitalize(venueRaw.toLowerCase(), ' ') + " ";
    }

    @Scheduled(cron = "${cron.shows}")
    public void tweetOnThisDayOrRandomShow() {
        try {
            ZonedDateTime today = OffsetDateTime.now().atZoneSameInstant(ZoneId.of("America/Los_Angeles"));
            Element show = getRandomShowForDate(today.getDayOfMonth(), today.getMonthValue(), null);
            if (show == null) {
                tweetRandomShow();
                return;
            }
            tweetTheShow(show, today);
        } catch (Exception e) {
            googliTweeter.tweet("Caught exception trying to post shows on this day!", e.getCause());
        }
    }

    private String addActualDate(Element element, String tweet) {
        String actualDate = element.getElementsByClass("setlist-date-long").get(0).getElementsByTag("a").get(1).wholeText();
        tweet += WordUtils.capitalize(actualDate.toLowerCase(), ' ') + "\n";
        return tweet;
    }

    private String addLink(Element element, String tweet) {
        String link = "phish.net" + element.getElementsByClass("setlist-date-long").get(0).getElementsByTag("a").get(1).attr("href");
        tweet += link + "\n\n";
        return tweet;
    }

    private String addLocation(Element element, String tweet) {
        String location = element.getElementsByClass("setlist-location").get(0).wholeText().replace("\n", "").replace("\t", "");
        tweet += location.replaceAll(",", ", ") + " " + " \n";
        return tweet;
    }

    private String addTotalShows(int totalShows, ZonedDateTime today, String tweet) {
        tweet += totalShows - 1 + " other shows on this date\n";
        tweet += "https://phish.net/setlists/?month=" + today.getMonthValue() + "&day=" + today.getDayOfMonth() + "\n";
        return tweet;
    }

    private String addVenue(Element element, String tweet) {
        String venue = getVenueOfShow(element);
        tweet += venue;
        return tweet;
    }

    private List<Element> getShowsFromResponse(String response) {
        Document doc = Jsoup.parse(response);
        int setlists = doc.getElementsByClass("setlist").size();

        if (setlists == 0) {
            return emptyList();
        }

        return doc.getElementsByClass("setlist");
    }

    private void tweetRandomShow() {
        LOG.warn("Found no shows on this day. Tweeting a random setlist...");
        String url = "https://phish.net/setlists/jump/random";
        String response = restTemplate.getForObject(url, String.class);
        Element show = getShowsFromResponse(response).get(0);
        String tweet = "#RandomShow\n";
        tweet = addActualDate(show, tweet);
        tweet = addVenue(show, tweet);
        tweet = addLocation(show, tweet);
        tweet = addLink(show, tweet);
        // TODO: add link to relisten, phish'n, phishtracks
        tweet = tweetWriter.addShowHashtags(tweet);
        twitterService.tweet(tweet);
    }

    private void tweetTheShow(Element show, ZonedDateTime today) {
        int setlists = show.getElementsByClass("setlist").size();
        String tweet = "#OnThisDay\n";
        tweet = addActualDate(show, tweet);
        tweet = addVenue(show, tweet);
        tweet = addLocation(show, tweet);
        tweet = addLink(show, tweet);
        // TODO: add link to relisten, phish'n, phishtracks
        if (setlists > 1) {
            tweet = addTotalShows(setlists, today, tweet);
        }
        tweet = tweetWriter.addShowHashtags(tweet);

        twitterService.tweet(tweet);
    }
}