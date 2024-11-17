package com.api.billmanager.presentation.dto.request;

import com.api.billmanager.presentation.dto.interfaces.Insert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserRequest {

    @NotNull(groups = {Insert.class}, message = "The 'id' field is required")
    private Long id;

    @NotBlank(groups = {Insert.class}, message = "The 'email' field is required")
    private String email;

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }
    
}
