package helpingfriendlybook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SongDTO {
    private String name;

    private String link;

    private String lastPlayed;

    private Integer gap;

    private Set<String> lyricSnippets = new HashSet<>();

    public Integer getGap() {
        return gap;
    }

    public String getLastPlayed() {
        return lastPlayed;
    }

    public String getName() {
        return name;
    }

    public void setGap(Integer gap) {
        this.gap = gap;
    }

    public void setLastPlayed(String lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Set<String> getLyricSnippets() {
        return lyricSnippets;
    }

    public void setLyricSnippets(Set<String> lyricSnippets) {
        this.lyricSnippets = lyricSnippets;
    }
}
