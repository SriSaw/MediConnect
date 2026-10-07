package com.mediconnect.message;

import com.mediconnect.appointment.Appointment;
import com.mediconnect.appointment.AppointmentRepository;
import com.mediconnect.common.PageResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.message.dto.ConversationSummaryResponse;
import com.mediconnect.message.dto.MessageResponse;
import com.mediconnect.message.dto.SendMessageRequest;
import com.mediconnect.security.UserPrincipal;
import com.mediconnect.user.User;
import com.mediconnect.user.UserRepository;
import com.mediconnect.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public MessageService(
            MessageRepository messageRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public MessageResponse sendMessage(UserPrincipal currentUser, SendMessageRequest request) {
        // Sender identity ALWAYS strictly bound to authenticated user
        Long senderId = currentUser.getId();

        if (senderId.equals(request.receiverId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));

        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.receiverId()));

        if (receiver.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Receiver account is not active");
        }

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository.findById(request.appointmentId())
                    .orElse(null);
        }

        Message message = new Message(
                sender,
                receiver,
                appointment,
                request.content().trim()
        );

        Message saved = messageRepository.save(message);
        return MessageResponse.from(saved);
    }

    @Transactional
    public PageResponse<MessageResponse> getConversation(UserPrincipal currentUser, Long otherUserId, Pageable pageable) {
        if (!userRepository.existsById(otherUserId)) {
            throw new ResourceNotFoundException("User", "id", otherUserId);
        }

        // Mark incoming messages from other user as read
        messageRepository.markConversationAsRead(currentUser.getId(), otherUserId);

        Page<Message> messages = messageRepository.findConversation(currentUser.getId(), otherUserId, pageable);
        return PageResponse.from(messages.map(MessageResponse::from));
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> getConversations(UserPrincipal currentUser) {
        List<Long> partnerIds = messageRepository.findDistinctConversationPartnerIds(currentUser.getId());
        List<ConversationSummaryResponse> summaries = new ArrayList<>();

        for (Long partnerId : partnerIds) {
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null) continue;

            Page<Message> lastMessagePage = messageRepository.findConversation(
                    currentUser.getId(), partnerId, PageRequest.of(0, 1)
            );

            String lastContent = "";
            java.time.LocalDateTime lastTime = null;
            if (!lastMessagePage.isEmpty()) {
                Message last = lastMessagePage.getContent().get(0);
                lastContent = last.getContent();
                lastTime = last.getCreatedAt();
            }

            summaries.add(new ConversationSummaryResponse(
                    partner.getId(),
                    partner.getName(),
                    partner.getRole(),
                    lastContent,
                    lastTime
            ));
        }

        return summaries;
    }
}
