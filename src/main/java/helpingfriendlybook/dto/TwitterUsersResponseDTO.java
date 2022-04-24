package helpingfriendlybook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterUsersResponseDTO {
    private Long next_cursor;

    private List<DataDTO> users;

    public Long getNext_cursor() {
        return next_cursor;
    }

    public void setNext_cursor(Long next_cursor) {
        this.next_cursor = next_cursor;
    }

    public List<DataDTO> getUsers() {
        return users;
    }

    public void setUsers(List<DataDTO> users) {
        this.users = users;
    }
}
