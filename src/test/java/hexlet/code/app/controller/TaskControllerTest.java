package hexlet.code.app.controller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;


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

        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);

        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testTaskStatus);
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
        Task newTask = Instancio.of(modelGenerator.getTaskModel()).create();

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTask));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByName(newTask.getName()).orElseThrow();

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(newTask.getName());
        assertThat(task.getTaskStatus()).isEqualTo(newTask.getTaskStatus());
        assertThat(task.getAssignee().getId()).isEqualTo(newTask.getAssignee().getId());
        assertThat(task.getDescription()).isEqualTo(newTask.getDescription());
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

        dto.setTitle("ToNewReview");
        dto.setStatus("to_new_review");

        var request = put("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());
//
        var task = taskRepository.findByName(testTask.getName()).orElseThrow();

        assertThat(task.getName()).isEqualTo(dto.getTitle());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(dto.getStatus());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        taskRepository.save(testTask);

        var dto = new HashMap<String, String>();
        dto.put("name", "AnotherName");

        var request = put("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var task = taskRepository.findByName(testTask.getName()).orElseThrow();

        assertThat(task.getTaskStatus().getSlug()).isEqualTo(testTask.getTaskStatus().getSlug());
        assertThat(task.getName()).isEqualTo(dto.get("name"));
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