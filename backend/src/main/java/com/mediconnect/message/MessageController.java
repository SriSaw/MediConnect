package com.mediconnect.message;

import com.mediconnect.common.PageResponse;
import com.mediconnect.message.dto.ConversationSummaryResponse;
import com.mediconnect.message.dto.MessageResponse;
import com.mediconnect.message.dto.SendMessageRequest;
import com.mediconnect.security.CurrentUser;
import com.mediconnect.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Message", description = "Secure REST-based patient-doctor messaging endpoints")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @Operation(summary = "Send a secure direct message")
    public ResponseEntity<MessageResponse> sendMessage(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageResponse response = messageService.sendMessage(currentUser, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/conversations")
    @Operation(summary = "List all active conversations for the authenticated user")
    public ResponseEntity<List<ConversationSummaryResponse>> getConversations(
            @CurrentUser UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(messageService.getConversations(currentUser));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get message thread with a specific user")
    public ResponseEntity<PageResponse<MessageResponse>> getConversation(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long userId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return ResponseEntity.ok(messageService.getConversation(currentUser, userId, pageable));
    }
}
