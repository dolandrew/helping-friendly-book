package helpingfriendlybook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataDTO {
    private Boolean followed_by;

    private Integer followers_count;

    private String id;

    private String author_id;

    private String name;

    private String screen_name;

    private String text;

    private String username;

    public String getAuthor_id() {
        return author_id;
    }

    public Boolean getFollowed_by() {
        return followed_by;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public void setFollowed_by(Boolean followed_by) {
        this.followed_by = followed_by;
    }

    public Integer getFollowersCount() {
        return followers_count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screen_name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFollowers_count(Integer followersCount) {
        this.followers_count = followersCount;
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }
}
