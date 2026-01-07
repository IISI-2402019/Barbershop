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
import java.util.List;

@Component
public class AppointmentReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderScheduler.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private LineNotificationService lineNotificationService;

    // Run every day at 08:00 PM (20:00) to remind appointments for TOMORROW
    // Or maybe 08:00 AM? User said "One day reminder". Usually sent the evening before or morning of.
    // Let's assume 08:00 PM the day before (or 20:00).
    // Cron expression: second, minute, hour, day of month, month, day of week
    @Scheduled(cron = "0 0 20 * * ?") 
    public void sendReminders() {
        logger.info("Starting scheduled appointment reminders...");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime startOfDay = tomorrow.atStartOfDay();
        LocalDateTime endOfDay = tomorrow.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository.findByStartTimeBetweenOrderByStartTime(startOfDay, endOfDay);

        for (Appointment appt : appointments) {
            if (appt.getStatus() == AppointmentStatus.BOOKED) {
                try {
                    lineNotificationService.sendAppointmentReminder(appt);
                } catch (Exception e) {
                    logger.error("Failed to send reminder for appointment ID: " + appt.getId(), e);
                }
            }
        }
        
        logger.info("Finished sending {} reminders.", appointments.size());
    }
}
