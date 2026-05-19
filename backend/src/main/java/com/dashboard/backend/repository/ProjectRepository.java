package com.dashboard.backend.repository;

import com.dashboard.backend.domain.Project;
import com.dashboard.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUser(User user);

    Optional<Project> findByTrackingKey(String trackingKey);

    boolean existsByTrackingKey(String trackingKey);
}
