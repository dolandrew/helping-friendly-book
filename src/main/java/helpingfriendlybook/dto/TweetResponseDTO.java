package helpingfriendlybook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TweetResponseDTO {
    private String id_str;

    public String getId() {
        return id_str;
    }

    public void setId(String id_str) {
        this.id_str = id_str;
    }
}
