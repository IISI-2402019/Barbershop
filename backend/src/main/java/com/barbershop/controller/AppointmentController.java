package com.barbershop.controller;

import com.barbershop.dto.AppointmentRequest;
import com.barbershop.model.*;
import com.barbershop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// Excel Export Imports
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

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

    @Autowired
    private ScheduleRepository scheduleRepository;

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

        // 5. Check Availability (Appointments overlap)
        List<Appointment> conflicts = appointmentRepository.findByStylistIdAndStartTimeBetween(
                stylist.getId(), start, end.minusSeconds(1)); // minus 1 sec to allow back-to-back

        if (!conflicts.isEmpty()) {
            return ResponseEntity.badRequest().body("Stylist is not available at this time (Appointment Conflict).");
        }

        // 6. Check Schedule Availability (Leave/Off-time overlap)
        // This now checks both the stylist's personal schedule AND global store schedules (stylist IS NULL)
        List<Schedule> scheduleConflicts = scheduleRepository.findOverlappingSchedules(
                stylist.getId(), end, start);

        if (!scheduleConflicts.isEmpty()) {
            return ResponseEntity.badRequest().body("Stylist is not available at this time (Schedule Conflict or Store Closed).");
        }

        // 7. Save Appointment
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        if (!appointmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        appointmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Validate Stylist
        Stylist stylist = stylistRepository.findById(request.getStylistId())
                .orElseThrow(() -> new RuntimeException("Stylist not found"));

        // Validate Service
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Calculate End Time
        LocalDateTime start = request.getStartTime();
        long durationMinutes = (long) (service.getDurationHours() * 60);
        LocalDateTime end = start.plusMinutes(durationMinutes);

        // Check Availability (excluding current appointment)
        List<Appointment> conflicts = appointmentRepository.findByStylistIdAndStartTimeBetween(
                stylist.getId(), start, end.minusSeconds(1));
        
        // Remove current appointment from conflicts check
        conflicts.removeIf(a -> a.getId().equals(id));

        if (!conflicts.isEmpty()) {
            return ResponseEntity.badRequest().body("Stylist is not available at this time.");
        }

        appointment.setStylist(stylist);
        appointment.setService(service);
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        
        Appointment updated = appointmentRepository.save(appointment);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public List<Appointment> getAllAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (start != null && end != null) {
            return appointmentRepository.findByStartTimeBetweenOrderByStartTime(start, end);
        }
        return appointmentRepository.findAll();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) throws IOException {

        List<Appointment> appointments = appointmentRepository.findByStartTimeBetweenOrderByStartTime(start, end);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Appointments");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Customer Name", "Phone Number", "Appointment Time", "Service", "Remarks"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Appointment appt : appointments) {
                Row row = sheet.createRow(rowNum++);
                String customerName = appt.getCustomer().getRealName() != null ? appt.getCustomer().getRealName() : appt.getCustomer().getDisplayName();
                row.createCell(0).setCellValue(customerName);
                row.createCell(1).setCellValue(appt.getCustomer().getPhone() != null ? appt.getCustomer().getPhone() : "");
                row.createCell(2).setCellValue(appt.getStartTime().format(formatter));
                row.createCell(3).setCellValue(appt.getService().getName());
                row.createCell(4).setCellValue(""); // Remarks (Empty)
            }

            // Autosize columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "appointments.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());
        }
    }
}
