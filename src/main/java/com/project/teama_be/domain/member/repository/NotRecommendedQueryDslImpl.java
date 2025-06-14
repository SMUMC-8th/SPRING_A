package com.project.teama_be.domain.member.repository;

import com.project.teama_be.domain.member.entity.QNotRecommended;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotRecommendedQueryDslImpl implements NotRecommendedQueryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    // 내가 차단한 유저 리스트 조회
    @Override
    public List<Long> findBlockingUserList(
            Long memberId
    ){
        QNotRecommended notRecommended = QNotRecommended.notRecommended;

        return jpaQueryFactory
                .select(notRecommended.targetMemberId)
                .from(notRecommended)
                .where(notRecommended.member.id.eq(memberId))
                .fetch();
    }

    // 날 차단한 유저 리스트 조회
    @Override
    public List<Long> findBlockerList(
            Long memberId
    ){
        QNotRecommended notRecommended = QNotRecommended.notRecommended;

        return jpaQueryFactory
                .select(notRecommended.member.id)
                .from(notRecommended)
                .where(notRecommended.targetMemberId.eq(memberId))
                .fetch();
    }
}
