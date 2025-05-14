package com.project.teama_be.domain.post.repository;

import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public interface CommentQueryDsl {

    // 댓글 목록 조회
    CommentResDTO.PageableComment<CommentResDTO.Comment> findCommentList(
            Predicate subQuery,
            int size
    );

    // 대댓글 목록 조회
    CommentResDTO.PageableComment<CommentResDTO.Reply> findReplyComments(
            Predicate subQuery,
            int size
    );

    // 내가 작성한 댓글 조회
    CommentResDTO.PageableComment<CommentResDTO.SimpleComment> getMyComments(
            Predicate builder,
            int size
    );
}
