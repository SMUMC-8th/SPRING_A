package com.project.teama_be.domain.post.service.query;

import com.project.teama_be.domain.member.exceptioin.MemberErrorCode;
import com.project.teama_be.domain.member.exceptioin.MemberException;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.entity.QComment;
import com.project.teama_be.domain.post.exception.CommentException;
import com.project.teama_be.domain.post.exception.code.CommentErrorCode;
import com.project.teama_be.domain.post.repository.CommentRepository;
import com.project.teama_be.global.security.userdetails.AuthUser;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;

    // 댓글 목록 조회 ✅
    public CommentResDTO.PageableComment<CommentResDTO.Comment> findComments(
            Long postId,
            String cursor,
            int size
    ) {
        // 조회할 객체 선언
        QComment comment = QComment.comment;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.post.id.eq(postId))
                // 대댓글 조회 방지
                .and(comment.parentId.eq(0L));

        if (!cursor.equals("-1")) {
            try {
                builder.and(comment.id.loe(Long.parseLong(cursor)));
            } catch (NumberFormatException e) {
                throw new CommentException(CommentErrorCode.NOT_VALID_CURSOR);
            }
        }
        log.info("[ 댓글 목록 조회 ] subQuery:{}", builder);
        return commentRepository.findCommentList(builder, size);
    }

    // 대댓글 목록 조회
    public CommentResDTO.PageableComment<CommentResDTO.Reply> findReplyComments(
            Long commentId,
            String cursor,
            int size
    ) {

        // 조회할 객체 선언
        QComment comment = QComment.comment;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.parentId.eq(commentId));

        if (!cursor.equals("-1")) {
            try{
                builder.and(comment.id.goe(Long.parseLong(cursor)));
            } catch (NumberFormatException e) {
                throw new CommentException(CommentErrorCode.NOT_VALID_CURSOR);
            }
        }

        log.info("[ 대댓글 목록 조회 ] subQuery:{}", builder);
        return commentRepository.findReplyComments(builder, size);
    }

    // 내가 작성한 댓글 조회 ✅
    public CommentResDTO.PageableComment<CommentResDTO.SimpleComment> findMyComments(
            Long memberId,
            AuthUser user,
            String cursor,
            int size
    ){
        // 로그인 유저와 memberID가 같은지 검증
        validateMember(user, memberId);

        // 조회할 객체 선언
        QComment comment = QComment.comment;

        // 조건 설정
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.member.id.eq(memberId));

        if (!cursor.equals("-1")) {
            try {
                builder.and(comment.id.loe(Long.parseLong(cursor)));
            } catch (NumberFormatException e) {
                throw new CommentException(CommentErrorCode.NOT_VALID_CURSOR);
            }
        }

        log.info("[ 내가 작성한 댓글 조회 ] subQuery:{}", builder);
        return commentRepository.getMyComments(builder, size);

    }

    private void validateMember(AuthUser user, Long memberId) {

        if (!user.getUserId().equals(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
    }
}
