package hexlet.code.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskUpdateDTO {

    private JsonNullable<Integer> index;

    @NotNull
    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    @NotBlank
    private JsonNullable<String> title;

    @NotBlank
    private JsonNullable<String> content;

    @NotBlank
    private JsonNullable<String> status;
}
