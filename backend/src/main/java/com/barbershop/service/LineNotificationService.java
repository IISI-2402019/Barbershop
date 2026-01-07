package com.barbershop.service;

import com.barbershop.model.Appointment;
import com.barbershop.model.User;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.PushMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class LineNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(LineNotificationService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired(required = false)
    private MessagingApiClient messagingApiClient;

    public void sendBookingSuccess(Appointment appointment) {
        if (messagingApiClient == null) return;
        
        User user = appointment.getCustomer();
        String message = String.format("""
            預約成功！
            
            時間: %s
            設計師: %s
            服務內容: %s
            
            期待您的光臨！""",
            appointment.getStartTime().format(FORMATTER),
            appointment.getStylist().getName(),
            appointment.getService().getName()
        );
        sendPushMessage(user.getLineUserId(), message);
    }

    public void sendAppointmentCancelled(Appointment appointment) {
        if (messagingApiClient == null) return;

        User user = appointment.getCustomer();
        String message = String.format("您的預約: %s 已被取消。",
            appointment.getStartTime().format(FORMATTER)
        );
        sendPushMessage(user.getLineUserId(), message);
    }

    public void sendAppointmentReminder(Appointment appointment) {
        if (messagingApiClient == null) return;

        User user = appointment.getCustomer();
        String message = String.format("""
            【預約提醒】
            
            提醒您，您明日有理髮預約。
            時間: %s
            設計師: %s
            服務: %s
            
            若需變更(當日)，請提前致電告知。""",
            appointment.getStartTime().format(FORMATTER),
            appointment.getStylist().getName(),
            appointment.getService().getName()
        );
        sendPushMessage(user.getLineUserId(), message);
    }

    private void sendPushMessage(String userId, String text) {
        if (userId == null || userId.isEmpty()) return;

        try {
            // In SDK 8.x, TextMessage takes a text string
            // PushMessageRequest takes (to, List<Message>, notificationDisabled, customAggregationUnits)
            // Or simpler constructor: (to, List<Message>) -- actually check builder or constructor
            
            // Using UUID for idempotency key if needed, or just standard call
            
            PushMessageRequest request = new PushMessageRequest(
                userId,
                List.of(new TextMessage(text)),
                false,
                null
            );

            messagingApiClient.pushMessage(UUID.randomUUID(), request).get();
            logger.info("Sent LINE message to {}", userId);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to send LINE message", e);
        }
    }
}
