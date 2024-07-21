package hexlet.code.app.component;

import hexlet.code.app.dto.TaskStatusCreateDTO;
import hexlet.code.app.model.User;
import hexlet.code.app.service.CustomUserDetailsService;
import hexlet.code.app.service.TaskStatusService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    private final TaskStatusService taskStatusService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var admin = new User();
        admin.setEmail("hexlet@example.com");
        admin.setPasswordDigest("qwerty");
        userDetailsService.createUser(admin);

        List<TaskStatusCreateDTO> taskStatuses = Arrays.asList(
                new TaskStatusCreateDTO("Draft", "draft"),
                new TaskStatusCreateDTO("ToReview", "to_review"),
                new TaskStatusCreateDTO("ToBeFixed", "to_be_fixed"),
                new TaskStatusCreateDTO("ToPublish", "to_publish"),
                new TaskStatusCreateDTO("Published", "published")
        );
        taskStatuses.forEach(taskStatusService::create);
    }
}
