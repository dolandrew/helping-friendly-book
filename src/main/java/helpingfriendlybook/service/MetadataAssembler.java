package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
import helpingfriendlybook.entity.SongEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataAssembler {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataAssembler.class);

    @Autowired
    private SongLoader songLoader;

    public SongDTO assembleMetadata(String songName) {
        LOG.info("Assembling metadata for: " + songName);
        SongDTO songDTO = new SongDTO();
        songDTO.setName(songName);
        List<SongEntity> currentSongEntityList = songLoader.getSongs().stream()
                .filter(song -> song.getNameLower().contains(songName.toLowerCase()))
                .collect(Collectors.toList());
        if (!currentSongEntityList.isEmpty()) {
            songDTO.setGap(currentSongEntityList.get(0).getGap());
            songDTO.setLastPlayed(currentSongEntityList.get(0).getLastPlayed());
            songDTO.setLink(currentSongEntityList.get(0).getLink());
        }
        LOG.info("Successfully assembled metadata.");
        return songDTO;
    }
}