package com.project.teama_be.domain.post.service.query;

import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.entity.QComment;
import com.project.teama_be.domain.post.repository.CommentRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;

    // 댓글 목록 조회
    public CommentResDTO.PageableComment<CommentResDTO.Comment> findComments(
            Long postId,
            Long cursor,
            int size
    ) {
        // 조회할 객체 선언
        QComment comment = QComment.comment;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.post.id.eq(postId))
                // 대댓글 조회 방지
                .and(comment.parentId.eq(0L));

        if (cursor != -1) {
            builder.and(comment.id.loe(cursor));
        }

        log.info("[ 댓글 목록 조회 ] 댓글 목록을 조회합니다.");
        return commentRepository.findCommentList(builder, size);
    }

    // 대댓글 목록 조회
    public CommentResDTO.PageableComment<CommentResDTO.Reply> findReplyComments(
            Long commentId,
            Long cursor,
            int size
    ) {
        // 조회할 객체 선언
        QComment comment = QComment.comment;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.parentId.eq(commentId));

        if (cursor != -1) {
            builder.and(comment.id.goe(cursor));
        }

        return commentRepository.findReplyComments(builder, size);
    }
}
