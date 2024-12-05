package com.api.billmanager.presentation.dto.request;

import com.api.billmanager.domain.model.User;
import com.api.billmanager.presentation.dto.interfaces.Insert;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class UserRequest {

    @NotBlank(groups = {Insert.class}, message = "The field 'email' is required")
    private String email;

    public User convertRequestToUser(){
        return User.builder().email(this.email).build();
    }
}
