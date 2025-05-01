package com.project.teama_be.global.security.dto;

import lombok.Builder;

@Builder
public record JwtDTO (

        String accessToken,

        String refreshToken
){

}
