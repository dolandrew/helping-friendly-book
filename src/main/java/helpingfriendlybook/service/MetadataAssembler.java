package helpingfriendlybook.service;

import helpingfriendlybook.dto.SongDTO;
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
        List<SongDTO> currentSongDTOList = songLoader.getSongs().stream()
                .filter(song -> song.getNameLower().contains(songName.toLowerCase()))
                .collect(Collectors.toList());
        if (!currentSongDTOList.isEmpty()) {
            songDTO.setGap(currentSongDTOList.get(0).getGap());
            songDTO.setLastPlayed(currentSongDTOList.get(0).getLastPlayed());
            songDTO.setLink(currentSongDTOList.get(0).getLink());
            songDTO.setTimes(currentSongDTOList.get(0).getTimes());
            songDTO.setDebut(currentSongDTOList.get(0).getDebut());
        }
        LOG.info("Successfully assembled metadata.");
        return songDTO;
    }
}