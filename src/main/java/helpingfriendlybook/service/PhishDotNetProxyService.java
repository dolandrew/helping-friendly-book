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

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpMethod.GET;

@Service
public class PhishDotNetProxyService {
    private static final Logger LOG = LoggerFactory.getLogger(PhishDotNetProxyService.class);

    private final RestTemplate restTemplate;

    public PhishDotNetProxyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static String getVenueOfShow(Element element) {
        String venueRaw = element
                .getElementsByClass("setlist-venue").get(0)
                .getElementsByTag("span").get(0)
                .wholeText();
        return WordUtils.capitalize(venueRaw.toLowerCase(), ' ') + " ";
    }

    public static Elements getShowsFromResponse(String response) {
        Document doc = Jsoup.parse(response);
        return doc.getElementsByClass("setlist");
    }

    public List<Element> getShowsForDate(Integer day, Integer month, Integer year) {
        LOG.info("Looking for shows on " + month + "-" + day + "...");
        String url = "https://phish.net/setlists/?month=" + month + "&day=" + day + (year != null ? "&year=" + year : "");
        List<Element> shows = getShowsWithAbbreviatedSetlists(url);
        LOG.info("HFB found " + shows.size() + " shows on " + (year != null ? year + "-" : "") + month + "-" + day + ".");
        return shows;
    }

    public Element getRandomShow() {
        String url = "https://phish.net/setlists/jump/random";
        return getShowsWithAbbreviatedSetlists(url).get(0);
    }

    public String getSongs() {
        return restTemplate.getForObject("https://phish.net/song", String.class);
    }

    private Elements getShowsWithAbbreviatedSetlists(String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Cookie", " songabbr=on; ");
        HttpEntity<HttpHeaders> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, GET, requestEntity, String.class);
        return getShowsFromResponse(response.getBody());
    }

    Document getShow(String url) {
        ResponseEntity<String> response = restTemplate.exchange("http://" + url, GET, null, String.class);
        return Jsoup.parse(response.getBody());
    }
}