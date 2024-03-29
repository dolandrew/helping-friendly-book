package helpingfriendlybook.service;

import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.GET;

@Service
public class PhishDotNetProxyService {
    private static final Logger LOG = LoggerFactory.getLogger(PhishDotNetProxyService.class);

    private final RestTemplate restTemplate;

    public PhishDotNetProxyService(final RestTemplate template) {
        this.restTemplate = template;
    }

    public static String getVenueOfShow(final Element element) {
        String venueRaw = element
                .getElementsByClass("setlist-venue").get(0)
                .getElementsByTag("span").get(0)
                .wholeText();
        return WordUtils.capitalize(venueRaw.toLowerCase(), ' ') + " ";
    }

    public static Elements getShowsFromResponse(final String response) {
        Document doc = Jsoup.parse(response);
        return doc.getElementsByClass("setlist");
    }

    public List<Element> getShowsForDate(final Integer day, final Integer month, final Integer year) {
        LOG.info("Looking for shows on " + month + "-" + day + "...");
        String url = "https://phish.net/setlists/?month=" + month + "&day=" + day + (year != null ? "&year=" + year : "");
        List<Element> shows = getShowsWithAbbreviatedSetlists(url);
        LOG.info("HFB found " + shows.size() + " shows on " + (year != null ? year + "-" : "") + month + "-" + day + ".");
        return shows;
    }

    public String getLastPlayedSongForDate(final Integer day,
                                          final Integer month,
                                      final Integer year) {
        LOG.info("Looking for setlist on " + month + "-" + day + "...");
        String url = "https://phish.net/setlists/?month=" + month + "&day=" + day + (year != null ? "&year=" + year : "");
        ResponseEntity<String> response = restTemplate.exchange(url, GET, null,
                String.class);
        List<Element> shows =  getShowsFromResponse(response.getBody());
        LOG.info("HFB found " + shows.size() + " shows on " + (year != null ? year + "-" : "") + month + "-" + day + ".");
        Element setlistBody = shows.get(0).getElementsByClass("setlist-body").get(0);
        List<Element> sets = setlistBody.getElementsByClass("set-label");
        List<String> songs =
                Objects.requireNonNull(sets.get(sets.size()-1).parent().getElementsByClass("setlist-song")).stream().map(element -> element.wholeText()).collect(Collectors.toList());

        return songs.get(songs.size()-1);
    }



    public Element getRandomShow() {
        String url = "https://phish.net/setlists/jump/random";
        return getShowsWithAbbreviatedSetlists(url).get(0);
    }

    public String getSongs() {
        return restTemplate.getForObject("https://phish.net/song", String.class);
    }

    private Elements getShowsWithAbbreviatedSetlists(final String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Cookie", " songabbr=on; ");
        HttpEntity<HttpHeaders> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, GET, requestEntity, String.class);
        return getShowsFromResponse(response.getBody());
    }

    Document getShow(final String url) {
        ResponseEntity<String> response = restTemplate.exchange("http://" + url, GET, null, String.class);
        return Jsoup.parse(response.getBody());
    }
}
