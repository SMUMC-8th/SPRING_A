package com.project.teama_be.domain.post.repository;


import com.project.teama_be.domain.post.converter.CommentConverter;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.entity.QComment;
import com.project.teama_be.domain.post.exception.CommentException;
import com.project.teama_be.domain.post.exception.code.CommentErrorCode;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentQueryDslImpl implements CommentQueryDsl{

    private final JPAQueryFactory jpaQueryFactory;

    // 댓글 목록 조회✅
    @Override
    public CommentResDTO.PageableComment<CommentResDTO.Comment> findCommentList(
            Predicate subQuery,
            int size
    ) {
        // 조회할 객체 선언
        QComment comment = QComment.comment;
        // 서브 쿼리 사용 시 따로 선언해줘야 함
        QComment child = new QComment("child");

        List<CommentResDTO.Comment> comments = jpaQueryFactory
                .from(comment)
                .where(subQuery)
                .orderBy(comment.id.desc())
                .groupBy(comment.id)
                .limit(size+1)
                .transform(GroupBy.groupBy(comment.id).list(
                        Projections.constructor(
                                CommentResDTO.Comment.class,
                                comment.id,
                                comment.member.nickname,
                                comment.member.profileUrl,
                                comment.content,
                                comment.likeCount,
                                select(child.count())
                                        .from(child)
                                        .where(child.parentId.eq(comment.id))
                        )
                ));

        // 게시글에 댓글이 존재하지 않는 경우
        if (comments.isEmpty()) {
            throw new CommentException(CommentErrorCode.NOT_FOUND_COMMENT);
        }

        // 페이징 정보 설정
        boolean hasNext = comments.size() > size;
        int pageSize = Math.min(comments.size(), size);
        String cursor = comments.get(comments.size()-1).commentId().toString();

        // 데이터 정제
        comments = comments.subList(0, pageSize);

        log.info("[ 댓글 목록 조회 ] resultCnt:{}, hasNext:{}, pageSize:{}, cursor:{}",
                comments.size(), hasNext, pageSize, cursor);
        return CommentConverter.toPageableComment(
                comments, hasNext, pageSize, cursor
        );
    }

    // 대댓글 목록 조회 ✅
    @Override
    public CommentResDTO.PageableComment<CommentResDTO.Reply> findReplyComments(
            Predicate subQuery,
            int size
    ) {
        // 조회할 객체 선언
        QComment comment = QComment.comment;

        List<CommentResDTO.Reply> comments = jpaQueryFactory
                .from(comment)
                .where(subQuery)
                .orderBy(comment.id.asc())
                .limit(size+1)
                .transform(GroupBy.groupBy(comment.id).list(
                        Projections.constructor(
                                CommentResDTO.Reply.class,
                                comment.id,
                                comment.member.nickname,
                                comment.member.profileUrl,
                                comment.content,
                                comment.likeCount
                        )
                ));

        // 대댓글이 없는 경우
        if (comments.isEmpty()) {
            throw new CommentException(CommentErrorCode.NOT_FOUND_REPLY);
        }

        // 페이징 정보 설정
        boolean hasNext = comments.size() > size;
        int pageSize = Math.min(comments.size(), size);
        String cursor = comments.get(comments.size()-1).commentId().toString();

        // 데이터 정제
        comments = comments.subList(0, pageSize);

        log.info("[ 대댓글 목록 조회 ] resultCnt:{}, hasNext:{}, pageSize:{}, cursor:{}",
                comments.size(), hasNext, pageSize, cursor);
        return CommentConverter.toPageableComment(
                comments, hasNext, pageSize, cursor
        );
    }

    // 내가 작성한 댓글 조회 ✅
    @Override
    public CommentResDTO.PageableComment<CommentResDTO.SimpleComment> getMyComments(
            Predicate subQuery,
            int size
    ) {

        // 조회할 객체 선언
        QComment comment = QComment.comment;

        List<CommentResDTO.SimpleComment> comments = jpaQueryFactory
                .from(comment)
                .where(subQuery)
                .orderBy(comment.id.desc())
                .limit(size+1)
                .transform(GroupBy.groupBy(comment.id).list(
                        Projections.constructor(
                                CommentResDTO.SimpleComment.class,
                                comment.id,
                                comment.content,
                                comment.likeCount
                        )
                ));

        // 작성한 댓글이 없는 경우
        if (comments.isEmpty()) {
            throw new CommentException(CommentErrorCode.NOT_FOUND_MY_COMMENT);
        }

        // 페이징 정보 설정
        boolean hasNext = comments.size() > size;
        int pageSize = Math.min(comments.size(), size);
        String cursor = comments.get(comments.size()-1).commentId().toString();

        // 데이터 정제
        comments = comments.subList(0, pageSize);

        log.info("[ 내가 작성한 댓글 조회 ] resultCnt:{}, hasNext:{}, pageSize:{}, cursor:{}",
                comments.size(), hasNext, pageSize, cursor);
        return CommentConverter.toPageableComment(
                comments, hasNext, pageSize, cursor
        );
    }
}
