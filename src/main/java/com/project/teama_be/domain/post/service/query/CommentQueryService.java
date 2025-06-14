package com.project.teama_be.domain.post.service.query;

import com.project.teama_be.domain.member.entity.QNotRecommended;
import com.project.teama_be.domain.member.repository.NotRecommendedRepository;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.entity.Comment;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.entity.QComment;
import com.project.teama_be.domain.post.exception.CommentException;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.CommentErrorCode;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.domain.post.repository.CommentRepository;
import com.project.teama_be.domain.post.repository.PostRepository;
import com.project.teama_be.global.security.userdetails.AuthUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final NotRecommendedRepository notRecommendedRepository;
    private final PostRepository postRepository;

    // 댓글 목록 조회 ✅
    public CommentResDTO.PageableComment<CommentResDTO.Comment> findComments(
            AuthUser user,
            Long postId,
            String cursor,
            int size
    ) {
        // 조회할 객체 선언
        QComment comment = QComment.comment;
        QNotRecommended notRecommended = QNotRecommended.notRecommended;

        // 게시글 주인이 날 차단했는지 확인: 게시글 주인과 차단한 상대가 다른 경우
        Post origin = postRepository.findPostById(postId).orElseThrow(()->
                new PostException(PostErrorCode.NOT_FOUND));
        isBlockedUser(user.getUserId(), origin.getMember().getId());

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.post.id.eq(postId))
                // 대댓글 조회 방지
                .and(comment.parentId.eq(0L))
                // 내가 차단한 사용자의 댓글 제외
                .and(comment.member.id.notIn(
                        JPAExpressions.select(notRecommended.targetMemberId)
                                .from(notRecommended)
                                .where(notRecommended.member.id.in(user.getUserId()))
                ))
                // 상대방이 차단한 경우, 상대방 댓글 제외
                .and(comment.member.id.notIn(
                        JPAExpressions.select(notRecommended.member.id)
                                .from(notRecommended)
                                .where(notRecommended.targetMemberId.in(user.getUserId()))
                ))
        ;

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
            AuthUser user,
            Long commentId,
            String cursor,
            int size
    ) {

        // 조회할 객체 선언
        QComment comment = QComment.comment;
        QNotRecommended notRecommended = QNotRecommended.notRecommended;

        // 댓글 주인이 날 차단했는지 확인
        Comment origin = commentRepository.findById(commentId).orElseThrow(() ->
                new CommentException(CommentErrorCode.NOT_FOUND));
        isBlockedUser(user.getUserId(), origin.getMember().getId());

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.parentId.eq(commentId))
                // 차단한 사용자의 댓글 제외
                .and(comment.member.id.notIn(
                JPAExpressions.select(notRecommended.targetMemberId)
                        .from(notRecommended)
                        .where(notRecommended.member.id.in(user.getUserId()))
                ))
                // 상대방이 차단한 경우, 상대방 댓글 제외
                .and(comment.member.id.notIn(
                        JPAExpressions.select(notRecommended.member.id)
                                .from(notRecommended)
                                .where(notRecommended.targetMemberId.in(user.getUserId()))
                ));

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
            AuthUser user,
            String cursor,
            int size
    ){

        // 조회할 객체 선언
        QComment comment = QComment.comment;

        // 조건 설정
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.member.id.eq(user.getUserId()));

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

    // 사용자가 날 차단했는지 확인
    private void isBlockedUser(Long userId, Long targetUserId) {
        List<Long> blackList = notRecommendedRepository.findBlockingUserList(targetUserId);
        if (blackList.contains(userId)) {
            throw new CommentException(CommentErrorCode.BLOCKING);
        }
    }
}
