package com.api.billmanager.presentation;

import com.api.billmanager.application.service.AuthenticationService;
import com.api.billmanager.presentation.controller.AuthenticationController;
import com.api.billmanager.presentation.dto.request.AuthenticationRequest;
import com.api.billmanager.presentation.dto.request.RegisterRequest;
import com.api.billmanager.presentation.dto.response.AuthenticationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockBean
    private final AuthenticationService authService;

    public AuthenticationControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, AuthenticationService authService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.authService = authService;
    }

    @Test
    public void testRegister() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("email@test.com");
        registerRequest.setPassword("password123");

        AuthenticationResponse mockResponse = new AuthenticationResponse("mock-jwt-token");

        Mockito.when(authService.register(Mockito.any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void testAuthenticate() throws Exception {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setEmail("email@test.com");
        authRequest.setPassword("password123");

        AuthenticationResponse mockResponse = new AuthenticationResponse("mock-jwt-token");

        Mockito.when(authService.authenticate(Mockito.any(AuthenticationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

}
