package com.api.billmanager.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.api.billmanager.domain.exception.UserNotFoundException;
import com.api.billmanager.domain.model.User;
import com.api.billmanager.infrastructure.persistence.UserRepositoryInterface;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryInterface repository;

    public User findByEmail(String email) throws UserNotFoundException{
        return this.repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email " + email
                + " not exist."));
    }

    public User findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id
                        + " not exist."));
    }
}
