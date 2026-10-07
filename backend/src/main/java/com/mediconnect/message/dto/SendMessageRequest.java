package com.mediconnect.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotNull(message = "Receiver ID is required")
        Long receiverId,

        Long appointmentId,

        @NotBlank(message = "Message content cannot be blank")
        @Size(max = 2000, message = "Message content cannot exceed 2000 characters")
        String content
) {}
