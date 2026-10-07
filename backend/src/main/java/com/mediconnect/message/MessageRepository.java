package com.mediconnect.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
           "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
           "ORDER BY m.createdAt ASC")
    Page<Message> findConversation(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id,
            Pageable pageable
    );

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.receiver.id ELSE m.sender.id END " +
           "FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Long> findDistinctConversationPartnerIds(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId")
    int markConversationAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
}
