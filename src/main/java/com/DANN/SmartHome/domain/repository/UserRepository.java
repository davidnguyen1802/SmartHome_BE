package com.DANN.SmartHome.domain.repository;

import com.DANN.SmartHome.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
