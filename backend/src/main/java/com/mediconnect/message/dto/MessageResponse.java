package com.mediconnect.message.dto;

import com.mediconnect.message.Message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long senderId,
        String senderName,
        Long receiverId,
        String receiverName,
        Long appointmentId,
        String content,
        boolean read,
        LocalDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        if (message == null) {
            return null;
        }
        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getReceiver().getId(),
                message.getReceiver().getName(),
                message.getAppointment() != null ? message.getAppointment().getId() : null,
                message.getContent(),
                message.isRead(),
                message.getCreatedAt()
        );
    }
}
