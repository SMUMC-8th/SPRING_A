package com.project.teama_be.domain.post.entity;

import com.project.teama_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_name", nullable = false, unique = true)
    @Size(max = 6, message = "각 태그는 6자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[가-힣]*$", message = "완성된 한글만 입력 가능합니다.")
    private String tagName;

}
