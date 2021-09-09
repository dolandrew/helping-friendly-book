package helpingfriendlybook.entity;

public class SongEntity {

    private String id;

    private String lastPlayed;

    private String name;

    private String nameLower;

    private String link;

    private String lyrics;

    private String lyricsBy;

    private Integer gap;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameLower() {
        return nameLower;
    }

    public void setNameLower(String nameLower) {
        this.nameLower = nameLower;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getLyricsBy() {
        return lyricsBy;
    }

    public void setLyricsBy(String lyricsBy) {
        this.lyricsBy = lyricsBy;
    }
}
