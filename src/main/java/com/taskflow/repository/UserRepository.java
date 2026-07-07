package com.taskflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskflow.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
