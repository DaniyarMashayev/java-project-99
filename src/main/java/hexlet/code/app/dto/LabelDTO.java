package hexlet.code.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelDTO {
    private long id;

    @NotNull
    private String name;

    private String createdAt;
}
