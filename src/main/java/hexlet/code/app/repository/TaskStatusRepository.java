package hexlet.code.app.repository;

import hexlet.code.app.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long>, JpaSpecificationExecutor<TaskStatus> {
    Optional<TaskStatus> findBySlug(String slug);
}
