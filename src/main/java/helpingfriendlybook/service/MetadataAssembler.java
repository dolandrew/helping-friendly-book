package helpingfriendlybook.service;

import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.SongDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataAssembler {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataAssembler.class);

    private final SongLoader songLoader;

    private final GoogliTweeter googliTweeter;

    private final TwitterService twitterService;

    private String currentSongName;

    public MetadataAssembler(SongLoader songLoader, GoogliTweeter googliTweeter, TwitterService twitterService) {
        this.songLoader = songLoader;
        this.googliTweeter = googliTweeter;
        this.twitterService = twitterService;
    }

    public SongDTO processTweet(ResponseEntity<TwitterResponseDTO> responseEntity) {
        TwitterResponseDTO body = responseEntity.getBody();
        if (body != null) {
            if (body.getData() != null) {
                DataDTO data = body.getData().get(0);
                String fetchedSongName = data.getText();
                String cleanedSongName = cleanSongName(fetchedSongName);
                if (sameTweet(cleanedSongName)) {
                    return null;
                }
                currentSongName = cleanedSongName;
                if (shouldIgnoreTweet(fetchedSongName)) {
                    twitterService.favoriteTweetById(responseEntity.getBody().getData().get(0).getId());
                    return null;
                }
                return assembleMetadata(cleanedSongName);
            } else {
                LOG.error("Found no tweets!");
            }
        }
        return null;
    }

    public SongDTO assembleMetadata(String cleanedSongName) {
        LOG.warn("Found new song: " + cleanedSongName);
        LOG.warn("Assembling metadata for: " + cleanedSongName);
        SongDTO songDTO = new SongDTO();
        songDTO.setName(cleanedSongName);
        List<SongDTO> currentSongDTOList = songLoader.getSongs().stream()
                .filter(song -> song.getNameLower().equals(cleanedSongName.toLowerCase()))
                .collect(Collectors.toList());
        if (!currentSongDTOList.isEmpty()) {
            SongDTO fetchedSong = currentSongDTOList.get(0);
            if (fetchedSong.getAliasOf() != null) {
                googliTweeter.tweet("HFB recognized " + fetchedSong.getName() + " as an alias of: " + fetchedSong.getAliasOf());
                return assembleMetadata(fetchedSong.getAliasOf());
            }
            songDTO.setName(fetchedSong.getName());
            songDTO.setGap(fetchedSong.getGap());
            songDTO.setLastPlayed(fetchedSong.getLastPlayed());
            songDTO.setLink(fetchedSong.getLink());
            songDTO.setTimes(fetchedSong.getTimes());
            songDTO.setDebut(fetchedSong.getDebut());
        } else {
            googliTweeter.tweet("HFB tried to assemble metadata but found no results");
        }
        LOG.warn("Successfully assembled metadata.");
        return songDTO;
    }

    private boolean sameTweet(String fetchedSongName) {
        if (fetchedSongName.equals(currentSongName)) {
            LOG.warn("No new tweets");
            return true;
        }
        return false;
    }

    private boolean shouldIgnoreTweet(String fetchedSongName)  {
        if (fetchedSongName == null) {
            LOG.warn("Skipping empty tweet");
            return true;

        }
        if (fetchedSongName.contains("\uD83D\uDCF8")
                || fetchedSongName.contains("@rene_huemer")
                || fetchedSongName.contains("https://t.co")
                || fetchedSongName.matches(".*[0-9]+/[0-9]+/[0-9]{4}.*")
        ) {
            LOG.warn("Skipping tweet: " + fetchedSongName);
            return true;
        }
        return false;
    }

    private String cleanSongName(String fetchedSongName) {
        return fetchedSongName
                .replaceAll("&gt; ", "")
                .replaceAll("&gt;&gt; ", "")
                .replaceAll("SET ONE: ", "")
                .replaceAll("SET TWO: ", "")
                .replaceAll("ENCORE: ", "");
    }

}