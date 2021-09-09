package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class SongLoader {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataAssembler.class);

    private final RestTemplate restTemplate;

    private String PHISH_NET_URL = "https://www.phish.net";

    public SongLoader(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<SongDTO> getSongs() {
        LOG.info("Fetching songs...");
        String response = restTemplate.getForObject(PHISH_NET_URL + "/song", String.class);
        Document doc = Jsoup.parse(response);
        Elements rows = doc.getElementsByTag("tr");
        List<SongDTO> songs = new ArrayList<>();
        for (Element element : rows.subList(1, rows.size())) {
            Elements cells = element.getElementsByTag("td");
            Element songNameCell = cells.get(0);
            String songName = songNameCell.wholeText();
            SongDTO songDTO = new SongDTO();
            songDTO.setLink(PHISH_NET_URL + songNameCell.getElementsByTag("a").attr("href"));
            songDTO.setName(songName);
            songDTO.setNameLower(songName.toLowerCase());

            try {
                if (cells.size() >= 3) {
                    songDTO.setTimes(Integer.valueOf(cells.get(2).wholeText()));
                }
            } catch (Exception e) {
                LOG.warn("Unable to understand times: " + cells.get(2));
            }
            if (cells.size() >= 4) {
                songDTO.setDebut(cells.get(3).wholeText());
            } else {
                songDTO.setDebut("unknown");
            }
            if (cells.size() >= 5) {
                songDTO.setLastPlayed(cells.get(4).wholeText());
            } else {
                songDTO.setLastPlayed("unknown");
            }
            if (cells.size() >= 6) {
                Element gap = cells.get(5);
                String cleanedGap = gap.wholeText().replaceAll("<td>", "").replaceAll("</td>", "");
                songDTO.setGap(Integer.valueOf(cleanedGap));
            }
            songs.add(songDTO);
        }
        LOG.info("Successfully fetched songs.");
        return songs;
    }
}