package helpingfriendlybook.service;

import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

import static helpingfriendlybook.service.PhishDotNetProxyService.getVenueOfShow;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class OnThisDayService {
    private static final Logger LOG = LoggerFactory.getLogger(OnThisDayService.class);

    private final GoogliTweeter googliTweeter;

    private final PhishDotNetProxyService phishDotNetProxyService;

    private final TweetWriter tweetWriter;

    private final TwitterService twitterService;

    public OnThisDayService(PhishDotNetProxyService phishDotNetProxyService,
                            TwitterService twitterService,
            GoogliTweeter googliTweeter,
            TweetWriter tweetWriter) {
        this.phishDotNetProxyService = phishDotNetProxyService;
        this.twitterService = twitterService;
        this.googliTweeter = googliTweeter;
        this.tweetWriter = tweetWriter;
    }

    @Scheduled(cron = "${cron.shows}")
    public void tweetOnThisDayOrRandomShow() {
        try {
            ZonedDateTime today = OffsetDateTime.now().atZoneSameInstant(ZoneId.of("America/Los_Angeles"));
            List<Element> shows = phishDotNetProxyService.getShowsForDate(today.getDayOfMonth(), today.getMonthValue(), null);
            if (!isEmpty(shows)) {
                int random = new Random().nextInt(shows.size());
                Element show = shows.get(random);
                tweetOnThisDay(show, null, "#OnThisDay\n");
            } else {
                tweetRandomShow();
            }
        } catch (Exception e) {
            googliTweeter.tweet("Caught exception trying to post shows on this day!", e.getCause());
        }
    }

    public String getSetlist(Element element) {
        Element setlistBody = element.getElementsByClass("setlist-body").get(0);
        List<Element> sets = setlistBody.getElementsByClass("set-label");
        String setlist = "";
        if ("SET 1".equals(sets.get(0).wholeText())) {
            setlist = getSetlistSet(sets, 0, setlist, "Set 1: ");
        }
        if (sets.size() > 1 && "SET 2".equals(sets.get(1).wholeText())) {
            setlist = getSetlistSet(sets, 1, setlist, "\nSet 2: ");
        } else if (sets.size() > 1 && "ENCORE".equals(sets.get(1).wholeText())) {
            setlist = getSetlistSet(sets, 1, setlist, "\nEncore: ");
        }
        if (sets.size() > 2 && "SET 3".equals(sets.get(2).wholeText())) {
            setlist = getSetlistSet(sets, 2, setlist, "\nSet 3: ");
        } else if (sets.size() > 2 && "ENCORE".equals(sets.get(2).wholeText())) {
            setlist = getSetlistSet(sets, 2, setlist, "\nEncore: ");
        }
        if (sets.size() > 3 && "ENCORE".equals(sets.get(3).wholeText())) {
            setlist = getSetlistSet(sets, 3, setlist, "\nEncore: ");
        }
        return setlist;
    }

    private String addActualDate(Element element, String tweet) {
        String actualDate = element
                .getElementsByClass("setlist-date-long").get(0)
                .getElementsByTag("a").get(1)
                .wholeText();
        tweet += WordUtils.capitalize(actualDate.toLowerCase(), ' ') + "\n";
        return tweet;
    }

    private String addLink(Element element, String tweet) {
        String link = "phish.net" + element
                .getElementsByClass("setlist-date-long").get(0)
                .getElementsByTag("a").get(1)
                .attr("href");
        tweet += link + "\n\n";
        return tweet;
    }

    private String addLocation(Element element, String tweet) {
        String location = element
                .getElementsByClass("setlist-location").get(0)
                .wholeText()
                .replace("\n", "")
                .replace("\t", "");
        tweet += location.replaceAll(",", ", ") + " " + " \n";
        return tweet;
    }

    private String addVenue(Element element, String tweet) {
        String venue = getVenueOfShow(element);
        tweet += venue;
        return tweet;
    }

    private String getSetlistNotes(Element element) {
        String setlistNotes = element.getElementsByClass("setlist-notes").get(0).wholeText();
        return setlistNotes.replace("NBSP", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace(" and ", " & ")
                .replace(" first ", " 1st ")
                .replace(" second ", " 2nd ")
                .replace(" third ", " 3rd ")
                .replace(" seven ", " 7 ")
                .replace(" three ", " 3 ")
                .replace(" eight ", " 8 ")
                .replace(" without ", " w/o ")
                .replace(" including ", " incl. ")
                .replace(" September ", " Sept. ")
                .replace(" October ", " Oct. ")
                .replace(" November ", " Nov. ")
                .replace(" December ", " Dec. ")
                .replace(" January ", " Jan. ")
                .replace(" February ", " Feb. ")
                .replace(" August ", " Aug. ")
                .replace(" unfinished ", " unf. ")
                .replace(" unfinished.", " unf.")
                .replace(" You Enjoy Myself ", " YEM ")
                .replace(" Down With Disease ", " DwD ")
                .replace(" Down with Disease ", " DwD ")
                .replace(" I Didn't Know ", " IDK ")
                .replace(" Backwards Down the Number Line ", " BDTNL ")
                .replace(" Backwards Down The Number Line ", " BDTNL ")
                .replace(" Wading in the Velvet Sea ", " Wading ")
                .replace("\r", "");
    }

    private String getSetlistSet(List<Element> sets, int index, String setlist, String setlist1) {
        List<Element> songs = sets.get(index).parent().getElementsByClass("setlist-song");
        setlist += setlist1;
        for (Element song : songs) {
            setlist += song.wholeText() + ", ";
        }
        setlist = setlist.substring(0, setlist.length() - 2);
        return setlist;
    }

    public void tweetOnThisDay(Element show, Long inReplyTo, String tweet) {
        LOG.warn("Tweeting OnThisDay...");
        if (inReplyTo != null) {
            tweetTheShowInReply(show, tweet, inReplyTo);
        } else {
            tweetTheShow(show, tweet, null);
        }
    }

    private void tweetRandomShow() {
        LOG.warn("Tweeting a random setlist...");
        Element show = phishDotNetProxyService.getRandomShow();
        String tweet = "#RandomShow\n";
        tweetTheShow(show, tweet, null);
    }

    private void tweetTheShow(Element show, String tweet, Long inReplyTo) {
        String username = tweet;
        tweet = addActualDate(show, tweet);
        tweet = addVenue(show, tweet);
        tweet = addLocation(show, tweet);
        tweet = addLink(show, tweet);
        // TODO: add link to relisten, phish'n, phishtracks
        tweet = tweetWriter.addShowHashtags(tweet);

        twitterService.tweet(username + getSetlistNotes(show), inReplyTo);
        twitterService.tweet(username + getSetlist(show), inReplyTo);
        twitterService.tweet(tweet, inReplyTo);
    }

    private void tweetTheShowInReply(Element show, String tweet, Long inReplyTo) {
        String username = tweet;
        tweet = addActualDate(show, tweet);
        tweet = addVenue(show, tweet);
        tweet = addLocation(show, tweet);
        tweet = addLink(show, tweet);
        // TODO: add link to relisten, phish'n, phishtracks
        tweet = tweetWriter.addShowReplyHashtags(tweet);

        twitterService.tweet(username + tweet, inReplyTo);
        twitterService.tweet(username + getSetlist(show), inReplyTo);
        twitterService.tweet(username + getSetlistNotes(show), inReplyTo);
    }
}