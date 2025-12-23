package com.barbershop.controller;

import com.barbershop.dto.AppointmentRequest;
import com.barbershop.model.*;
import com.barbershop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StylistRepository stylistRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request) {
        // 1. Validate User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate Stylist
        Stylist stylist = stylistRepository.findById(request.getStylistId())
                .orElseThrow(() -> new RuntimeException("Stylist not found"));

        // 3. Validate Service
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // 4. Calculate End Time
        LocalDateTime start = request.getStartTime();
        long durationMinutes = (long) (service.getDurationHours() * 60);
        LocalDateTime end = start.plusMinutes(durationMinutes);

        // 5. Check Availability (Simple overlap check)
        List<Appointment> conflicts = appointmentRepository.findByStylistIdAndStartTimeBetween(
                stylist.getId(), start, end.minusSeconds(1)); // minus 1 sec to allow back-to-back

        if (!conflicts.isEmpty()) {
            return ResponseEntity.badRequest().body("Stylist is not available at this time.");
        }

        // 6. Save Appointment
        Appointment appointment = new Appointment(user, stylist, service, start, end, AppointmentStatus.BOOKED);
        Appointment saved = appointmentRepository.save(appointment);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/my")
    public List<Appointment> getMyAppointments(@RequestParam Long userId) {
        return appointmentRepository.findByCustomerIdOrderByStartTimeDesc(userId);
    }
    
    @GetMapping("/stylist/{stylistId}")
    public List<Appointment> getStylistAppointments(@PathVariable Long stylistId) {
        // In a real app, you might want to filter by date range
        return appointmentRepository.findByStylistIdOrderByStartTime(stylistId);
    }
}
