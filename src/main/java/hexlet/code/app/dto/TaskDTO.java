package hexlet.code.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDTO {

    private long id;

    private Integer index;

    private String createdAt;

    @JsonProperty("assignee_id")
    private long assigneeId;

    private String title;

    private String content;

    private String status;
}
