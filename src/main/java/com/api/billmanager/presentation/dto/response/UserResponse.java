package com.api.billmanager.presentation.dto.response;

import com.api.billmanager.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;

    private String email;

    public UserResponse convertUserToResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

}
