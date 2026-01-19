package com.barbershop.scheduler;

import com.barbershop.model.Appointment;
import com.barbershop.model.AppointmentStatus;
import com.barbershop.repository.AppointmentRepository;
import com.barbershop.service.LineNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.barbershop.model.User;
import com.barbershop.repository.UserRepository;

@Component
public class AppointmentReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderScheduler.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private LineNotificationService lineNotificationService;

    @Autowired
    private UserRepository userRepository;

    // Run every day at 14:00 PM (Taiwan Time)
    @Scheduled(cron = "0 0 14 * * ?", zone = "Asia/Taipei") 
    public void sendReminders() {
        logger.info("Starting scheduled appointment reminders...");
        
        // 1. Next Day Reminders (Existing Logic)
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime startOfDay = tomorrow.atStartOfDay();
        LocalDateTime endOfDay = tomorrow.atTime(LocalTime.MAX);

        List<Appointment> nextDayAppointments = appointmentRepository.findByStartTimeBetweenOrderByStartTime(startOfDay, endOfDay);

        for (Appointment appt : nextDayAppointments) {
            if (appt.getStatus() == AppointmentStatus.BOOKED) {
                try {
                    lineNotificationService.sendAppointmentReminder(appt);
                } catch (Exception e) {
                    logger.error("Failed to send reminder for appointment ID: " + appt.getId(), e);
                }
            }
        }
        
        // 2. Cycle Reminders
        sendCycleReminders();

        logger.info("Finished sending {} reminders.", nextDayAppointments.size());
    }

    private void sendCycleReminders() {
        logger.info("Starting cycle reminders...");
        List<User> usersWithCycle = userRepository.findByReminderCycleIsNotNull();
        
        LocalDate today = LocalDate.now();

        for (User user : usersWithCycle) {
            try {
                // If cycle is <= 0 or too crazy, skip
                if (user.getReminderCycle() <= 0) continue;

                // Check if user has ANY future appointments (if so, no need to remind)
                if (appointmentRepository.existsByCustomerIdAndStartTimeAfter(user.getId(), LocalDateTime.now())) {
                    continue;
                }

                // Get last appointment
                Appointment lastAppt = appointmentRepository.findFirstByCustomerIdOrderByStartTimeDesc(user.getId());
                if (lastAppt == null) continue;
                
                // Only consider completed or booked (past) appointments as reference? 
                // Actually even CANCELED might count if they haven't cut hair? 
                // Let's stick to BOOKED/COMPLETED history.
                if (lastAppt.getStatus() == AppointmentStatus.CANCELED) continue;

                LocalDate lastDate = lastAppt.getStartTime().toLocalDate();
                long daysSince = ChronoUnit.DAYS.between(lastDate, today);
                
                // Target: lastDate + (cycle_weeks * 7) - 7 days (1 week before due)
                // If today == lastDate + (cycle_weeks * 7) - 7
                // Equivalent: daysSince == (cycle_weeks * 7) - 7
                
                long cycleInDays = user.getReminderCycle() * 7L;
                long triggerDay = cycleInDays - 7;
                
                if (daysSince == triggerDay && triggerDay > 0) {
                     lineNotificationService.sendCycleReminder(user);
                     logger.info("Sent cycle reminder to user: {}", user.getId());
                }

            } catch (Exception e) {
                logger.error("Failed to process cycle reminder for user: " + user.getId(), e);
            }
        }
    }
}
