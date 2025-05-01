package com.project.teama_be.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "userTerm")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class UserTerm {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "service", nullable = false)
    private String service;

    // Todo : 약관 정리해서 필드 추가하기
    @Column(name = "Field")
    private String field;
}
