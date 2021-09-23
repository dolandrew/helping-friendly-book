package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataAssembler {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataAssembler.class);

    private final SongLoader songLoader;

    private final GoogliTweeter googliTweeter;

    public MetadataAssembler(SongLoader songLoader, GoogliTweeter googliTweeter) {
        this.songLoader = songLoader;
        this.googliTweeter = googliTweeter;
    }

    public SongDTO assembleMetadata(String songName) {
        LOG.warn("Assembling metadata for: " + songName);
        SongDTO songDTO = new SongDTO();
        songDTO.setName(songName);
        List<SongDTO> currentSongDTOList = songLoader.getSongs().stream()
                .filter(song -> song.getNameLower().contains(songName.toLowerCase()))
                .collect(Collectors.toList());
        if (!currentSongDTOList.isEmpty()) {
            if (currentSongDTOList.size() > 1) {

                String tweet = "Cannot choose between: ";
                for (SongDTO song : currentSongDTOList) {
                    tweet += song.getName() + ", ";
                }
                googliTweeter.tweet(tweet);
                return null;
            }
            SongDTO fetchedSong = currentSongDTOList.get(0);
            songDTO.setName(fetchedSong.getName());
            songDTO.setGap(fetchedSong.getGap());
            songDTO.setLastPlayed(fetchedSong.getLastPlayed());
            songDTO.setLink(fetchedSong.getLink());
            songDTO.setTimes(fetchedSong.getTimes());
            songDTO.setDebut(fetchedSong.getDebut());
        }
        LOG.warn("Successfully assembled metadata.");
        return songDTO;
    }
}