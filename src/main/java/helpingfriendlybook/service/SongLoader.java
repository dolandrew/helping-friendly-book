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

import javax.annotation.PostConstruct;
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

    @PostConstruct
    public List<SongDTO> getSongs() {
        LOG.info("Fetching songs...");
        String response = restTemplate.getForObject(PHISH_NET_URL + "/song", String.class);
        Document doc = Jsoup.parse(response);
        Elements rows = doc.getElementsByTag("tr");
        List<SongDTO> songs = new ArrayList<>();
        for (Element element : rows.subList(1, rows.size())) {
            SongDTO songDTO = new SongDTO();
            try {
                Elements cells = element.getElementsByTag("td");
                Element songNameCell = cells.get(0);
                String songName = songNameCell.wholeText();
                songDTO.setLink(PHISH_NET_URL + songNameCell.getElementsByTag("a").attr("href"));
                songDTO.setName(songName);
                songDTO.setNameLower(songName.toLowerCase());

                if (cells.get(2).wholeText().contains("Found in Discography")) {
                    LOG.warn(songName + "has never been played. It is only found in discography.");
                    continue;
                }
                if (cells.get(2).wholeText().contains("Alias of")) {
                    LOG.warn(songName + " is an alias.");
                    //TODO handle alias
                    continue;
                }
                songDTO.setTimes(Integer.valueOf(cells.get(2).wholeText()));
                songDTO.setDebut(cells.get(3).wholeText());
                songDTO.setLastPlayed(cells.get(4).wholeText());
                Element gap = cells.get(5);
                String cleanedGap = gap.wholeText().replaceAll("<td>", "").replaceAll("</td>", "");
                songDTO.setGap(Integer.valueOf(cleanedGap));
                songs.add(songDTO);
            } catch (Exception e) {
                LOG.error("Caught exception while processing song: " + songDTO.getName() , e);
                throw e;
            }
        }
        LOG.info("Successfully fetched songs.");
        return songs;
    }
}