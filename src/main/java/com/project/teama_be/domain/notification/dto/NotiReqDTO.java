package com.project.teama_be.domain.notification.dto;

public class NotiReqDTO {

    public record FcmToken(
            String fcmToken
    ) {
    }
//    public record FcmSender(
//            String fcmToken,
//            String title,
//            String body
//    ) {
//    }
}
