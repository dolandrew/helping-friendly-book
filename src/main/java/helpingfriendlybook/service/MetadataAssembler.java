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
        LOG.warn("Found new song: " + songName);
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
            songDTO.setName(fetchedSong.getName());
            songDTO.setGap(fetchedSong.getGap());
            songDTO.setLastPlayed(fetchedSong.getLastPlayed());
            songDTO.setLink(fetchedSong.getLink());
            songDTO.setTimes(fetchedSong.getTimes());
            songDTO.setDebut(fetchedSong.getDebut());
        } else {
            googliTweeter.tweet("HFB tried to assemble metadata for " + songName + " but found no results.");
        }
        LOG.warn("Successfully assembled metadata for: " + songName + "");
        return songDTO;
    }



}