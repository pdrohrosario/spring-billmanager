package com.api.billmanager.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.billmanager.domain.model.User;

public interface UserRepositoryInterface extends JpaRepository<User, Integer> {
    
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);
}