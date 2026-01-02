package com.barbershop.controller;

import com.barbershop.dto.ScheduleRequest;
import com.barbershop.model.Schedule;
import com.barbershop.model.Stylist;
import com.barbershop.repository.ScheduleRepository;
import com.barbershop.repository.StylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private StylistRepository stylistRepository;

    @GetMapping
    public List<Schedule> getAllSchedules(@RequestParam(required = false) Long stylistId) {
        if (stylistId != null) {
            return scheduleRepository.findByStylistId(stylistId);
        }
        return scheduleRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleRequest request) {
        Stylist stylist = null;
        if (request.getStylistId() != null) {
            stylist = stylistRepository.findById(request.getStylistId())
                    .orElseThrow(() -> new RuntimeException("Stylist not found"));
        }

        Schedule schedule = new Schedule(
                stylist,
                request.getStartTime(),
                request.getEndTime(),
                request.getIsAllDay(),
                request.getReason()
        );

        return ResponseEntity.ok(scheduleRepository.save(schedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        if (!scheduleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        scheduleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
