package com.api.billmanager.presentation.dto.request;

import com.api.billmanager.presentation.dto.interfaces.Insert;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(groups = {Insert.class}, message = "The 'email' field is required")
    private String email;

    @NotBlank(groups = {Insert.class}, message = "The 'password' field is required")
    private String password;
    
}
