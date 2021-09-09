package helpingfriendlybook.service;

import helpingfriendlybook.entity.SongEntity;
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
import java.util.UUID;

@Component
public class SongLoader {

    private Logger log = LoggerFactory.getLogger(MetadataAssembler.class);

    @Autowired
    private RestTemplate restTemplate;

    private String PHISH_NET_URL = "https://www.phish.net";

    public List<SongEntity> getSongs() {
        log.info("Fetching songs...");
        String response = restTemplate.getForObject(PHISH_NET_URL + "/song", String.class);
        Document doc = Jsoup.parse(response);
        Elements rows = doc.getElementsByTag("tr");
        List<SongEntity> songs = new ArrayList<>();
        for (Element element : rows.subList(1, rows.size())) {
            Elements cells = element.getElementsByTag("td");
            Element songNameCell = cells.get(0);
            String songName = songNameCell.wholeText();
            SongEntity songEntity = new SongEntity();
            songEntity.setId(UUID.randomUUID().toString());
            songEntity.setLink(PHISH_NET_URL + songNameCell.getElementsByTag("a").attr("href"));
            songEntity.setName(songName);
            songEntity.setNameLower(songName.toLowerCase());

            if (cells.size() < 6) {
                continue;
            }
            Element gap = cells.get(5);
            String cleanedGap = gap.wholeText().replaceAll("<td>", "").replaceAll("</td>", "");
            songEntity.setGap(Integer.valueOf(cleanedGap));
            songEntity.setLastPlayed(cells.get(4).wholeText());
            songs.add(songEntity);
        }
        log.info("Successfully fetched songs.");
        return songs;
    }
}