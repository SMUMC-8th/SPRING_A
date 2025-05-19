package com.project.teama_be.global.security.dto;

import com.project.teama_be.domain.chat.dto.response.ChatResDTO;
import lombok.Builder;

public class AuthResDTO {

    @Builder
    public record Login(
            Long memberId,
            String accessToken,
            String refreshToken,
            String nickname,
            String profileUrl,
            ChatResDTO.SendBirdTokenInfo sendBirdToken
    ) {
    }
}
