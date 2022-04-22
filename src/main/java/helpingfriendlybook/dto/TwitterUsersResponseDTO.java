package helpingfriendlybook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterUsersResponseDTO {
    private List<DataDTO> users;

    private Long next_cursor;

    public Long getNext_cursor() {
        return next_cursor;
    }

    public List<DataDTO> getUsers() {
        return users;
    }

    public void setNext_cursor(Long next_cursor) {
        this.next_cursor = next_cursor;
    }

    public void setUsers(List<DataDTO> users) {
        this.users = users;
    }
}
