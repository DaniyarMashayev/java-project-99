package hexlet.code.controller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import hexlet.code.dto.TaskCreateDTO;
import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;


@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private Task testTask;
    private User testUser;
    private TaskStatus testTaskStatus;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        Label testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);

        TaskStatus testStatus = taskStatusRepository.findBySlug("draft").orElseThrow();

        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testStatus);
        Set<Label> labels = new HashSet<>();
        labels.add(testLabel);
        testTask.setLabels(labels);
    }


    @AfterEach
    public void clean() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        labelRepository.deleteAll();
    }

    @Test
    public void testShow() throws Exception {
        taskRepository.save(testTask);

        var request = get("/api/tasks/{id}", testTask.getId()).with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug())
        );
    }

    @Test
    public void testIndex() throws Exception {
        taskRepository.save(testTask);

        var result = mockMvc.perform(get("/api/tasks").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {

        var dto = new TaskCreateDTO();
        dto.setTitle(testTask.getName());
        dto.setContent(testTask.getDescription());
        dto.setIndex(testTask.getIndex());
        dto.setAssigneeId(testTask.getAssignee().getId());
        dto.setStatus(testTask.getTaskStatus().getSlug());

        mockMvc.perform(post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
                        .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

        var task = taskRepository.findByName(dto.getTitle()).orElseThrow();

        Assertions.assertThat(task.getName()).isEqualTo(dto.getTitle());
        Assertions.assertThat(task.getDescription()).isEqualTo(dto.getContent());
        Assertions.assertThat(task.getIndex()).isEqualTo(dto.getIndex());
        assertThat(task.getAssignee().getId()).isEqualTo(dto.getAssigneeId());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(dto.getStatus());
    }

    @Test
    public void testCreateWithNotValidTitle() throws Exception {
        var dto = taskMapper.map(testTask);
        dto.setTitle("");

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        taskRepository.save(testTask);

        var dto = taskMapper.map(testTask);

        dto.setStatus("to_review");
        dto.setContent("New Content");
        dto.setTitle("New Title");

        var request = put("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());
        var task = taskRepository.findByName(dto.getTitle()).orElseThrow();

        Assertions.assertThat(task.getName()).isEqualTo(dto.getTitle());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(dto.getStatus());
        Assertions.assertThat(task.getDescription()).isEqualTo(dto.getContent());
        assertThat(task.getAssignee().getUsername()).isEqualTo(testTask.getAssignee().getUsername());
        Assertions.assertThat(task.getLabels().size()).isEqualTo(testTask.getLabels().size());
    }


    @Test
    public void testDelete() throws Exception {
        taskRepository.save(testTask);

        var request = delete("/api/tasks/{id}", testTask.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(testTask.getId())).isEqualTo(false);
    }
}
