package helpingfriendlybook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FriendshipDTO {
    private RelationshipDTO relationship;

    public RelationshipDTO getRelationship() {
        return relationship;
    }

    public void setRelationship(RelationshipDTO relationship) {
        this.relationship = relationship;
    }
}
