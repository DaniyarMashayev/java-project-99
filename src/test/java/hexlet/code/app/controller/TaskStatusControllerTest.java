package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
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

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private TaskStatus testTaskStatus;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
    }

    @Test
    public void testShow() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var request = get("/api/task_statuses/{id}", testTaskStatus.getId()).with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug())
        );
    }

    @Test
    public void testIndex() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var result = mockMvc.perform(get("/api/task_statuses").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        TaskStatus newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskStatus));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(newTaskStatus.getSlug()).orElseThrow();

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getName()).isEqualTo(newTaskStatus.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(newTaskStatus.getSlug());
    }

    @Test
    public void testCreateWithNotValidName() throws Exception {
        var dto = taskStatusMapper.map(testTaskStatus);
        dto.setName("");

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var dto = taskStatusMapper.map(testTaskStatus);

        dto.setName("ToNewReview");
        dto.setSlug("to_new_review");

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElseThrow();

        assertThat(taskStatus.getName()).isEqualTo(dto.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(dto.getSlug());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var dto = new HashMap<String, String>();
        dto.put("name", "AnotherName");

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElseThrow();

        assertThat(taskStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
        assertThat(taskStatus.getName()).isEqualTo(dto.get("name"));
    }

    @Test
    public void testDelete() throws Exception {
        taskStatusRepository.save(testTaskStatus);

        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

    }
}
