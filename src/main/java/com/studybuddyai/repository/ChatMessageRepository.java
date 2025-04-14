package com.studybuddyai.repository;

import com.studybuddyai.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserIdOrderByTimestampAsc(Long userId);

    List<ChatMessage> findByDocumentIdOrderByTimestampAsc(Long documentId);

    @Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.document.id = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);
}