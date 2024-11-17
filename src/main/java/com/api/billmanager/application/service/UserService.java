package com.api.billmanager.application.service;

import org.springframework.stereotype.Service;

import com.api.billmanager.domain.exception.UserNotFoundException;
import com.api.billmanager.domain.model.User;
import com.api.billmanager.infrastructure.persistence.UserRepositoryInterface;

@Service
public class UserService {

    private final UserRepositoryInterface repository;

    public UserService(UserRepositoryInterface repository) {
        this.repository = repository;
    }

    public boolean validateUserExist(String email){
        return this.repository.findByEmail(email).isPresent();
    }

    public User findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id
                        + " not exist."));
    }
}
