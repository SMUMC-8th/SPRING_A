package com.project.teama_be.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.sdk.path}")
    private String secretFileName;

    @PostConstruct
    public void initialize() {
        System.out.println("FirebaseConfig: secretFileName = " + secretFileName);
        try {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(secretFileName).getInputStream());

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(googleCredentials)
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
