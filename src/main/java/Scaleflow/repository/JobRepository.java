package Scaleflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import Scaleflow.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
}
