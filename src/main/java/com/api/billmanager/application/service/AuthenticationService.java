package com.api.billmanager.application.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.billmanager.domain.enums.Role;
import com.api.billmanager.domain.exception.UserAlreadyExistException;
import com.api.billmanager.domain.model.User;
import com.api.billmanager.infrastructure.config.JwtService;
import com.api.billmanager.infrastructure.persistence.UserRepositoryInterface;
import com.api.billmanager.presentation.dto.request.AuthenticationRequest;
import com.api.billmanager.presentation.dto.request.RegisterRequest;
import com.api.billmanager.presentation.dto.response.AuthenticationResponse;

@Service
public class AuthenticationService {

    private final UserRepositoryInterface repository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepositoryInterface repository, PasswordEncoder passwordEncoder, JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        if(this.repository.findByEmail(request.getEmail()).isPresent()){
            throw new UserAlreadyExistException("User with 'email' : " + request.getEmail() + " already exist.");
        }
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()),Role.USER);
        repository.save(user);
        return buildAuthenticationResponse(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        return buildAuthenticationResponse(user);
    }
    
    private AuthenticationResponse buildAuthenticationResponse(User user){
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}
