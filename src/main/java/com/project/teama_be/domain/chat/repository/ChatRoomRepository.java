package com.project.teama_be.domain.chat.repository;

import com.project.teama_be.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByLocationId(Long locationId);

    /**
     * 지역명으로 미참여 채팅방 찾기
     * - 위치의 주소명, 도로명 주소, 장소명에 지역명이 포함되고
     * - 해당 사용자가 참여하지 않은 채팅방 조회
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.location l " +
            "WHERE (l.addressName LIKE %:region% OR l.roadAddressName LIKE %:region% OR l.placeName LIKE %:region%) " +
            "AND cr.id NOT IN (SELECT p.chatRoom.id FROM ChatParticipant p WHERE p.member.id = :memberId)")
    List<ChatRoom> findNonParticipatingRoomsByRegion(
            @Param("region") String region,
            @Param("memberId") Long memberId,
            Pageable pageable);
}
