package com.project.teama_be.domain.member.repository;

import java.util.List;

public interface NotRecommendedQueryDsl {
    // 차단한 유저 ID 조회
    List<Long> findBlockingUserList(
            Long memberId
    );
}
