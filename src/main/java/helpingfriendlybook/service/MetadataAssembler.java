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
        LOG.warn("Assembling metadata for: " + songName);
        SongDTO songDTO = new SongDTO();
        songDTO.setName(songName);
        List<SongDTO> currentSongDTOList = songLoader.getSongs().stream()
                .filter(song -> song.getNameLower().equals(songName.toLowerCase()))
                .collect(Collectors.toList());
        if (!currentSongDTOList.isEmpty()) {
            SongDTO fetchedSong = currentSongDTOList.get(0);
            if (fetchedSong.getAliasOf() != null) {
                String message = "HFB recognized " + fetchedSong.getName() + " as an alias of: " + fetchedSong.getAliasOf();
                googliTweeter.tweet(message);
                return assembleMetadata(fetchedSong.getAliasOf());
            }
            if (fetchedSong.getLastPlayed() != null) {
                String[] dateParts = fetchedSong.getLastPlayed().split("-");
                Element show = onThisDayService.getRandomShowForDate(Integer.valueOf(dateParts[2]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[0]));
                String venue = onThisDayService.getVenueOfShow(show);
                songDTO.setLastPlayed(fetchedSong.getLastPlayed() + " at " + venue);
            }
            songDTO.setName(fetchedSong.getName());
            songDTO.setGap(fetchedSong.getGap());
            songDTO.setLink(fetchedSong.getLink());
            songDTO.setTimes(fetchedSong.getTimes());
            if (fetchedSong.getDebut() != null) {
                String[] dateParts = fetchedSong.getDebut().split("-");
                Element show = onThisDayService.getRandomShowForDate(Integer.valueOf(dateParts[2]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[0]));
                String venue = onThisDayService.getVenueOfShow(show);
                songDTO.setDebut(fetchedSong.getDebut() + " at " + venue);
            }
        } else {
            googliTweeter.tweet("HFB tried to assemble metadata for " + songName + " but found no results. Assuming this is a debut.");
        }
        LOG.warn("Successfully assembled metadata for: " + songName + "");
        return songDTO;
    }
}