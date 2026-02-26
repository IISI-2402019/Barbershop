
package com.barbershop.controller;

import com.barbershop.dto.ScheduleRequest;
import com.barbershop.model.Schedule;
import com.barbershop.model.Stylist;
import com.barbershop.repository.ScheduleRepository;
import com.barbershop.repository.StylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import com.barbershop.model.User;
import com.barbershop.repository.UserRepository;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private StylistRepository stylistRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Schedule> getAllSchedules(@RequestParam(required = false) Long stylistId) {
        if (stylistId != null) {
            return scheduleRepository.findByStylistId(stylistId);
        }
        return scheduleRepository.findAll();
    }


    @GetMapping("/unavailable-dates")
    public List<String> getUnavailableDates(@RequestParam Long stylistId) {
        // Fetch all future schedules for this stylist (and global ones) that are marked as 'isAllDay'
        // Just fetching from "now" onwards for date picker validation
        LocalDateTime now = LocalDateTime.now().minusDays(1); // include today
        List<Schedule> unavailableSchedules = scheduleRepository.findFutureUnavailableSchedules(stylistId, now);

        List<String> disabledDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Schedule s : unavailableSchedules) {
            // Expand date range if multi-day
            LocalDateTime start = s.getStartTime();
            LocalDateTime end = s.getEndTime();
            
            LocalDateTime current = start;
            while (current.isBefore(end) || current.isEqual(end)) {
                disabledDates.add(current.format(formatter));
                current = current.plusDays(1);
            }
        }
        return disabledDates.stream().distinct().collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleRequest request,
            @RequestParam(required = false) Long userId) {
        Stylist stylist = null;

        // Validation: If userId is provided, check if it's a STYLIST and owns the
        // schedule
        if (userId != null) {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("使用者不存在"));
            if ("STYLIST".equals(user.getRole())) {
                Stylist loggedInStylist = stylistRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("此帳號未綁定設計師資料"));

                // If stylistId is provided in request, it MUST match the logged-in stylist
                if (request.getStylistId() != null && !request.getStylistId().equals(loggedInStylist.getId())) {
                    return ResponseEntity.status(403).body("您只能新增自己的排班");
                }
                // Force the stylistId to be the logged-in stylist
                request.setStylistId(loggedInStylist.getId());
            }
        }

        if (request.getStylistId() != null) {
            stylist = stylistRepository.findById(request.getStylistId())
                    .orElseThrow(() -> new RuntimeException("找不到指定的設計師"));
        } else {
            // Only Admin can create Store Closed (stylist == null)
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && !"ADMIN".equals(user.getRole())) {
                    return ResponseEntity.status(403).body("權限不足：僅管理員可設定店休");
                }
            }
        }

        Schedule schedule = new Schedule(
                stylist,
                request.getStartTime(),
                request.getEndTime(),
                request.getIsAllDay(),
                request.getReason());

        return ResponseEntity.ok(scheduleRepository.save(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody ScheduleRequest request,
            @RequestParam(required = false) Long userId) {
        return scheduleRepository.findById(id)
                .map(schedule -> {
                    // Validation
                    if (userId != null) {
                        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("使用者不存在"));
                        if ("STYLIST".equals(user.getRole())) {
                            Stylist loggedInStylist = stylistRepository.findByUserId(userId)
                                    .orElseThrow(() -> new RuntimeException("此帳號未綁定設計師資料"));

                            // Can only edit own schedule
                            if (schedule.getStylist() == null
                                    || !schedule.getStylist().getId().equals(loggedInStylist.getId())) {
                                throw new RuntimeException("您無權限修改此排班");
                            }
                            // Cannot change who the schedule belongs to (and must stay as self)
                            request.setStylistId(loggedInStylist.getId());
                        }
                    }

                    Stylist stylist = null;
                    if (request.getStylistId() != null) {
                        stylist = stylistRepository.findById(request.getStylistId())
                                .orElseThrow(() -> new RuntimeException("找不到指定的設計師"));
                    }
                    schedule.setStylist(stylist);
                    schedule.setStartTime(request.getStartTime());
                    schedule.setEndTime(request.getEndTime());
                    schedule.setIsAllDay(request.getIsAllDay());
                    schedule.setReason(request.getReason());
                    return ResponseEntity.ok(scheduleRepository.save(schedule));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        Schedule schedule = scheduleRepository.findById(id).orElse(null);
        if (schedule == null) {
            return ResponseEntity.notFound().build();
        }

        if (userId != null) {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("使用者不存在"));
            if ("STYLIST".equals(user.getRole())) {
                Stylist loggedInStylist = stylistRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("此帳號未綁定設計師資料"));

                if (schedule.getStylist() == null || !schedule.getStylist().getId().equals(loggedInStylist.getId())) {
                    return ResponseEntity.status(403).build();
                }
            }
        }

        scheduleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
