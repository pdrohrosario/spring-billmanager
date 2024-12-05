package com.api.billmanager.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;

}
