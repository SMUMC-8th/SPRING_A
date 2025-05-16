package com.project.teama_be.domain.post.converter;

import com.project.teama_be.domain.post.entity.Tag;

public class TagConverter {

    // 단일 태그 생성 : tagName -> Tag
    public static Tag toTag(
            String tagName
    ){
        return Tag.builder()
                .tagName(tagName)
                .build();
    }
}
