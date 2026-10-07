package com.mediconnect.message.dto;

import com.mediconnect.user.Role;

import java.time.LocalDateTime;

public record ConversationSummaryResponse(
        Long partnerId,
        String partnerName,
        Role partnerRole,
        String lastMessage,
        LocalDateTime lastMessageAt
) {}
