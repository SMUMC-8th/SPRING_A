package com.project.teama_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing //JPA Auditing 기능 활성화
@SpringBootApplication
public class TeamABeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamABeApplication.class, args);
    }

}
