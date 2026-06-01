package com.dashboard.backend.repository;

import com.dashboard.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDelYn(String email, String delYn);

    boolean existsByEmailAndDelYn(String email, String delYn);

    List<User> findByNameAndDelYn(String name, String delYn);
}
