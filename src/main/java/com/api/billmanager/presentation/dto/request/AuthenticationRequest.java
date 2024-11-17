package com.api.billmanager.presentation.dto.request;

import com.api.billmanager.presentation.dto.interfaces.Insert;

import jakarta.validation.constraints.NotBlank;

public class AuthenticationRequest {

    @NotBlank(groups = {Insert.class}, message = "The 'email' field is required")
    private String email;

    @NotBlank(groups = {Insert.class}, message = "The 'password' field is required")
    private String password;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    
}
