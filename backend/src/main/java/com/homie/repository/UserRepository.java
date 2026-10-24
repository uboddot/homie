package com.homie.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.homie.model.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
