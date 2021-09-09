package helpingfriendlybook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SongDTO {
    private String debut;

    private String name;

    private String link;

    private String lastPlayed;

    private Integer gap;

    private String nameLower;

    private Integer times;

    public String getDebut() {
        return debut;
    }

    public Integer getGap() {
        return gap;
    }

    public String getLastPlayed() {
        return lastPlayed;
    }

    public String getName() {
        return name;
    }

    public String getNameLower() {
        return nameLower;
    }

    public Integer getTimes() {
        return times;
    }

    public void setDebut(String debut) {
        this.debut = debut;
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

    public void setNameLower(String nameLower) {
        this.nameLower = nameLower;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }
}
