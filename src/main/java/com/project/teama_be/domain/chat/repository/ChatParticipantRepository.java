package com.project.teama_be.domain.chat.repository;

import com.project.teama_be.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);
}
