package hexlet.code.app.service;

import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.dto.TaskFilterDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public TaskDTO create(TaskCreateDTO taskCreateDTO) {
        var task = taskMapper.map(taskCreateDTO);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public List<TaskDTO> getAll() {
        var task = taskRepository.findAll();
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
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task With Id: " + id + " Not Found"));
        taskMapper.update(taskUpdateDTO, task);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
