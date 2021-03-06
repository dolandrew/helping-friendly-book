package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataAssembler {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataAssembler.class);

    private final GoogliTweeter googliTweeter;

    private final OnThisDayService onThisDayService;

    private final SongLoader songLoader;

    public MetadataAssembler(SongLoader songLoader, GoogliTweeter googliTweeter, OnThisDayService onThisDayService) {
        this.songLoader = songLoader;
        this.googliTweeter = googliTweeter;
        this.onThisDayService = onThisDayService;
    }

    public SongDTO assembleMetadata(String songName) {
        String cleanedSongName = songName.replaceAll("\\(|\\)|,", "");
        LOG.warn("Assembling metadata for: " + cleanedSongName);
        SongDTO songDTO = new SongDTO();
        songDTO.setName(cleanedSongName);
        List<SongDTO> currentSongDTOList = songLoader.getSongs().stream()
                .filter(song -> song.getNameLower().replaceAll("\\(|\\)|,", "").equals(cleanedSongName.toLowerCase()))
                .collect(Collectors.toList());
        if (!currentSongDTOList.isEmpty()) {
            SongDTO fetchedSong = currentSongDTOList.get(0);
            if (fetchedSong.getAliasOf() != null) {
                String message = "HFB recognized " + fetchedSong.getName() + " as an alias of: " + fetchedSong.getAliasOf();
                googliTweeter.tweet(message);
                return assembleMetadata(fetchedSong.getAliasOf());
            }
            if (fetchedSong.getLastPlayed() != null && !fetchedSong.getLastPlayed().equals("—")) {
                String[] dateParts = fetchedSong.getLastPlayed().split("-");
                List<Element> shows = onThisDayService.getShowsForDate(Integer.valueOf(dateParts[2]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[0]));
                String venue = onThisDayService.getVenueOfShow(shows.get(0));
                songDTO.setLastPlayed(fetchedSong.getLastPlayed() + " at " + venue);
            }
            songDTO.setName(fetchedSong.getName());
            songDTO.setGap(fetchedSong.getGap());
            songDTO.setLink(fetchedSong.getLink());
            songDTO.setTimes(fetchedSong.getTimes());
            if (fetchedSong.getDebut() != null && !fetchedSong.getDebut().equals("—")) {
                String[] dateParts = fetchedSong.getDebut().split("-");
                List<Element> shows = onThisDayService.getShowsForDate(Integer.valueOf(dateParts[2]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[0]));
                String venue = onThisDayService.getVenueOfShow(shows.get(0));
                songDTO.setDebut(fetchedSong.getDebut() + " at " + venue);
            }
        } else {
            googliTweeter.tweet("HFB tried to assemble metadata for " + cleanedSongName + " but found no results. Assuming this is a debut.");
        }
        LOG.warn("Successfully assembled metadata for: " + cleanedSongName + "");
        return songDTO;
    }
}