package helpingfriendlybook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationshipDTO {
    private DataDTO source;

    private DataDTO target;

    public DataDTO getSource() {
        return source;
    }

    public void setSource(DataDTO source) {
        this.source = source;
    }

    public DataDTO getTarget() {
        return target;
    }

    public void setTarget(DataDTO target) {
        this.target = target;
    }
}
