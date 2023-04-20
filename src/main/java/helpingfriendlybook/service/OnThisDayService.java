package helpingfriendlybook.service;

import helpingfriendlybook.dto.TweetResponseDTO;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static helpingfriendlybook.service.PhishDotNetProxyService.getVenueOfShow;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public final class OnThisDayService {
    private static final Logger LOG = LoggerFactory.getLogger(OnThisDayService.class);

    private final GoogliTweeter googliTweeter;

    private final PhishDotNetProxyService phishDotNetProxyService;

    private final TwitterService twitterService;

    public OnThisDayService(final PhishDotNetProxyService proxyService,
                            final TwitterService ts,
                            final GoogliTweeter googli) {
        this.phishDotNetProxyService = proxyService;
        this.twitterService = ts;
        this.googliTweeter = googli;
    }

    @Scheduled(cron = "${cron.shows}")
    public void tweetOnThisDayOrRandomShow() {
        boolean alwaysRandom = false; // for testing
        try {
            ZonedDateTime today = OffsetDateTime.now().atZoneSameInstant(ZoneId.of("America/Los_Angeles"));
            List<Element> shows = phishDotNetProxyService.getShowsForDate(today.getDayOfMonth(), today.getMonthValue(), null);
            if (!alwaysRandom && !isEmpty(shows)) {
                int random = new Random().nextInt(shows.size());
                Element show = shows.get(random);
                // TODO: filter out empty setlist or choose any
                // "No known setlist".equals(element.getElementsByClass("setlist-body").get(0).getElementsByTag("p").get(0).wholeText())
                tweetOnThisDay(show, null, "#OnThisDay\n");
            } else {
                LOG.warn("Tweeting a #RandomShow...");
                Element show = phishDotNetProxyService.getRandomShow();
                tweetTheShow(show, "#RandomShow\n", null);
            }
        } catch (Exception e) {
            googliTweeter.tweet("Caught exception trying to post show on this day.", e);
        }
    }

    public void tweetOnThisDay(final Element show, final String inReplyTo, final String tweet) {
        LOG.warn("Tweeting OnThisDay...");
        tweetTheShow(show, tweet, inReplyTo);
    }

    private void tweetTheShow(final Element show, final String tweet, final String inReplyTo) {
        StringBuilder builder = new StringBuilder(tweet)
                .append(getDate(show)).append("\n")
                .append(getVenueOfShow(show)).append(getLocation(show)).append("\n");

        String setlistLink = getSetlistLink(show);
        builder.append(setlistLink).append("\n\n");
        Document doc = phishDotNetProxyService.getShow(setlistLink);
        builder.append(getRating(doc)).append("\n")
                .append(getListenLink(doc)).append("\n").append("\n");

        builder.append(getShowHashtags());

        TweetResponseDTO tweetResponseDTO = twitterService.tweet(builder.toString(), inReplyTo);
        try {
            tweetResponseDTO = twitterService.tweet(getSetlist(show), tweetResponseDTO.getId());
            twitterService.tweet(getSetlistNotes(show), tweetResponseDTO.getId());
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("TODO: fix bug - shows on this day without setlist");
        }
    }

    private String getShowHashtags() {
        return "#phish #phishcompanion #otd" + OffsetDateTime.now().getYear();
    }

    private String getSetlist(final Element element) {
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

    private String getDate(final Element element) {
        String actualDate = element
                .getElementsByClass("setlist-date-long").get(0)
                .getElementsByTag("a").get(1)
                .wholeText().replaceAll("[a-zA-Z ]", "");
        if (actualDate.startsWith("0")) {
            actualDate = actualDate.substring(1);
        }
        return WordUtils.capitalize(actualDate.toLowerCase(), ' ');
    }

    private String getSetlistLink(final Element element) {
        return "phish.net" + element
                .getElementsByClass("setlist-date-long").get(0)
                .getElementsByTag("a").get(1)
                .attr("href");
    }

    private String getLocation(final Element element) {
        String location = element
                .getElementsByClass("setlist-location").get(0)
                .wholeText()
                .replace("\n", "")
                .replace("\t", "");
        return location.replaceAll(",", ", ") + " ";
    }

    private String getSetlistNotes(final Element element) {
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
                .replace(" tease ", " tz ")
                .replace(" tease.", " tz.")
                .replace(" teases ", " tz's ")
                .replace(" teases.", " tz's.")
                .replace(" teased ", " tz'd ")
                .replace(" teased.", " tz'd.")
                .replace(" You Enjoy Myself ", " YEM ")
                .replace(" Down With Disease ", " DwD ")
                .replace(" Down with Disease ", " DwD ")
                .replace(" I Didn't Know ", " IDK ")
                .replace(" Backwards Down the Number Line ", " BDTNL ")
                .replace(" Backwards Down The Number Line ", " BDTNL ")
                .replace(" Wading in the Velvet Sea ", " Wading ")
                .replace("\r", "");
    }

    private String getSetlistSet(final List<Element> sets, final int index, String setlist, final String setlist1) {
        List<Element> songs = Objects.requireNonNull(sets.get(index).parent()).getElementsByClass("setlist-song");
        setlist += setlist1;
        for (Element song : songs) {
            setlist += song.wholeText() + ", ";
        }
        setlist = setlist.substring(0, setlist.length() - 2);
        return setlist;
    }

    private String getRating(final Document doc) {
        try {
            return doc.getElementsByClass("permalink-rating").get(0).child(1).wholeText().replaceAll("Overall: ", "⭐ ");
        } catch (Exception e) {
            return "";
        }
    }

    private String getListenLink(final Document doc) {
        try {
            String listenLink = doc.getElementsByClass("linktrack").get(0).attr("href");
            if (!listenLink.contains("phish.in")) {
                return "";
            }
            return listenLink;
        } catch (Exception e) {
            return "";
        }
    }
}
