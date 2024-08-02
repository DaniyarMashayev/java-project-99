package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.dto.TaskFilterDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSpecification taskSpecification;


    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    public TaskDTO create(TaskCreateDTO taskCreateDTO) {
       try {
           var task = taskMapper.map(taskCreateDTO);

           var assignee = task.getAssignee();
           if (assignee != null) {
               assignee.addTask(task);
           }

           var status = task.getTaskStatus();
           status.addTask(task);

           var labels = task.getLabels();
           labels.forEach(label -> label.addTask(task));

           taskRepository.save(task);
           return taskMapper.map(task);

       } catch (NoSuchElementException ex) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
       }
    }

    public List<TaskDTO> getAll() {
        return taskRepository.findAll().stream()
                .map(taskMapper::map).toList();
    }

    public List<TaskDTO> getAll(TaskFilterDTO taskFilterDTO) {
        var filter = taskSpecification.build(taskFilterDTO);
        var tasks = taskRepository.findAll(filter);
        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task With Id: " + id + " Not Found"));
        return taskMapper.map(task);
    }

    public TaskDTO update(TaskUpdateDTO taskUpdateDTO, Long id) {
        try {
            var task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task With Id: " + id + " Not Found"));
            taskMapper.update(taskUpdateDTO, task);

            var assignee = task.getAssignee();
            if (assignee != null) {
                userRepository.findByEmail(assignee.getEmail())
                        .ifPresent(user -> user.addTask(task));
            }
            taskStatusRepository.findBySlug(task.getTaskStatus().getSlug())
                    .ifPresent(status -> status.addTask(task));

            Set<Long> labelIds = task.getLabels().stream()
                    .map(Label::getId)
                    .collect(Collectors.toSet());

            Set<Label> labels = labelRepository.findByIdIn(labelIds);
            labels.forEach(label -> label.addTask(task));


            taskRepository.save(task);
            return taskMapper.map(task);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
