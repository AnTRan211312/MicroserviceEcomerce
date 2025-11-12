package com.ecomerce.authservice.dto.response.auth;

import com.ecomerce.authservice.dto.response.user.UserSessionResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonPropertyOrder({"user", "accessToken"})
public class AuthTokenResponseDto {

    @JsonProperty("user")
    private UserSessionResponseDto userSessionResponseDto;
    private String accessToken;

}