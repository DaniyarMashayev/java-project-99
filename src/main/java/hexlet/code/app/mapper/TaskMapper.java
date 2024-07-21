package hexlet.code.app.mapper;

import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus", qualifiedByName = "slugToTaskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    public abstract Task map(TaskCreateDTO taskCreateDTO);


    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "taskStatus", target = "status")
    @Mapping(source = "assignee", target = "assigneeId")
    public abstract TaskDTO map(Task task);


    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "status", target = "taskStatus", qualifiedByName = "slugToTaskStatus")
    public abstract void update(TaskUpdateDTO taskUpdateDTO, @MappingTarget Task task);


    @Named("slugToTaskStatus")
    public TaskStatus slugToTaskStatus(String slug) {
        return taskStatusRepository.findBySlug(slug).orElseThrow(
                () -> new ResourceNotFoundException("TaskStatus with slug " + slug + " not found"));
    }
}