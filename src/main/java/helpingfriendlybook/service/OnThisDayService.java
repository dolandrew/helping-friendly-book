package helpingfriendlybook.service;

import helpingfriendlybook.dto.TweetResponseDTO;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.util.CollectionUtils.isEmpty;

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

    public String getSetlist(Element element) {
        Element setlistBody = element.getElementsByClass("setlist-body").get(0);
        List<Element> sets = setlistBody.getElementsByClass("set-label");
        String setlist = "";
        if ("SET 1".equals(sets.get(0).wholeText())) {
            List<Element> songs = sets.get(0).parent().getElementsByClass("setlist-song");
            setlist += "Set 1: ";
            for (Element song : songs) {
                setlist += song.wholeText() + ", ";
            }
            setlist = setlist.substring(0, setlist.length() - 2);
        }
        if (sets.size() > 1 && "SET 2".equals(sets.get(1).wholeText())) {
            List<Element> songs = sets.get(1).parent().getElementsByClass("setlist-song");
            setlist += "\nSet 2: ";
            for (Element song : songs) {
                setlist += song.wholeText() + ", ";
            }
            setlist = setlist.substring(0, setlist.length() - 2);
        } else if (sets.size() > 1 && "ENCORE".equals(sets.get(1).wholeText())) {
            List<Element> songs = sets.get(1).parent().getElementsByClass("setlist-song");
            setlist += "\nEncore: ";
            for (Element song : songs) {
                setlist += song.wholeText() + ", ";
            }
            setlist = setlist.substring(0, setlist.length() - 2);
        }
        if (sets.size() > 2 && "SET 3".equals(sets.get(2).wholeText())) {
            List<Element> songs = sets.get(2).parent().getElementsByClass("setlist-song");
            setlist += "\nSet 3: ";
            for (Element song : songs) {
                setlist += song.wholeText() + ", ";
            }
            setlist = setlist.substring(0, setlist.length() - 2);
        } else if (sets.size() > 2 && "ENCORE".equals(sets.get(2).wholeText())) {
            List<Element> songs = sets.get(2).parent().getElementsByClass("setlist-song");
            setlist += "\nEncore: ";
            for (Element song : songs) {
                setlist += song.wholeText() + ", ";
            }
            setlist = setlist.substring(0, setlist.length() - 2);
        }
        if (sets.size() > 3 && "ENCORE".equals(sets.get(3).wholeText())) {
            List<Element> songs = sets.get(3).parent().getElementsByClass("setlist-song");
            setlist += "\nEncore: ";
            for (Element song : songs) {
                setlist += song.wholeText() + ", ";
            }
            setlist = setlist.substring(0, setlist.length() - 2);
        }
        return setlist;
    }

    public List<Element> getShowsForDate(Integer day, Integer month, Integer year) {
        LOG.warn("Looking for shows on " + month + "-" + day + "...");
        String url = "https://phish.net/setlists/?month=" + month + "&day=" + day + (year != null ? "&year=" + year : "");
        List<Element> shows = getShowsWithAbbreviatedSetlists(url);
        LOG.warn("HFB found " + shows.size() + " shows on " + (year != null ? year + "-" : "") + month + "-" + day + ".");
        return shows;
    }

    public String getVenueOfShow(Element element) {
        String venueRaw = element.getElementsByClass("setlist-venue").get(0).getElementsByTag("span").get(0).wholeText();
        return WordUtils.capitalize(venueRaw.toLowerCase(), ' ') + " ";
    }

    @Scheduled(cron = "${cron.shows}")
    public void tweetOnThisDayOrRandomShow() {
        try {
            ZonedDateTime today = OffsetDateTime.now().atZoneSameInstant(ZoneId.of("America/Los_Angeles"));
            List<Element> shows = getShowsForDate(today.getDayOfMonth(), today.getMonthValue(), null);
            if (!isEmpty(shows)) {
                int random = new Random().nextInt(shows.size());
                Element show = shows.get(random);
                tweetOnThisDay(show, today, shows.size());
            } else {
                tweetRandomShow();
            }
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

    private String getSetlistNotes(Element element) {
        String setlistNotes = element.getElementsByClass("setlist-notes").get(0).wholeText();
        return setlistNotes.replace("NBSP", "").replace("\n", "").replace("\t", "").replace("\r", "");
    }

    private List<Element> getShowsFromResponse(String response) {
        Document doc = Jsoup.parse(response);
        int setlists = doc.getElementsByClass("setlist").size();

        if (setlists == 0) {
            return emptyList();
        }

        return doc.getElementsByClass("setlist");
    }

    private List<Element> getShowsWithAbbreviatedSetlists(String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Cookie", " songabbr=on; ");
        HttpEntity<HttpHeaders> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, GET, requestEntity, String.class);
        return getShowsFromResponse(response.getBody());
    }

    private void tweetOnThisDay(Element show, ZonedDateTime today, int size) {
        LOG.warn("Tweeting OnThisDay...");
        String tweet = "#OnThisDay\n";
        tweetTheShow(show, today, size, tweet);
    }

    private void tweetRandomShow() {
        LOG.warn("Tweeting a random setlist...");
        String url = "https://phish.net/setlists/jump/random";
        Element show = getShowsWithAbbreviatedSetlists(url).get(0);
        String tweet = "#RandomShow\n";
        tweetTheShow(show, null, 1, tweet);
    }

    private void tweetTheShow(Element show, ZonedDateTime today, int setlists, String tweet) {
        tweet = addActualDate(show, tweet);
        tweet = addVenue(show, tweet);
        tweet = addLocation(show, tweet);
        tweet = addLink(show, tweet);
        // TODO: add link to relisten, phish'n, phishtracks
//        if (setlists > 1) {
//            tweet = addTotalShows(setlists, today, tweet);
//        }
        tweet = tweetWriter.addShowHashtags(tweet);

        TweetResponseDTO tweetResponseDTO = twitterService.tweet(tweet);

        tweetResponseDTO = twitterService.tweet(getSetlist(show), tweetResponseDTO.getId());

        twitterService.tweet(getSetlistNotes(show), tweetResponseDTO.getId());
    }
}