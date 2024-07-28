package hexlet.code.component;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;
    private final TaskStatusService taskStatusService;
    private final LabelService labelService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var admin = new UserCreateDTO();
        admin.setEmail("hexlet@example.com");
        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setPassword("qwerty");
        userService.create(admin);

        List<TaskStatusCreateDTO> taskStatuses = Arrays.asList(
                new TaskStatusCreateDTO("Draft", "draft"),
                new TaskStatusCreateDTO("ToReview", "to_review"),
                new TaskStatusCreateDTO("ToBeFixed", "to_be_fixed"),
                new TaskStatusCreateDTO("ToPublish", "to_publish"),
                new TaskStatusCreateDTO("Published", "published")
        );
        taskStatuses.forEach(taskStatusService::create);

        List<LabelCreateDTO> label = List.of(
                new LabelCreateDTO("feature"),
                new LabelCreateDTO("bug")
        );
        label.forEach(labelService::create);
    }
}
