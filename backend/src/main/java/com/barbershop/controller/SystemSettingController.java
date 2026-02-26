package com.barbershop.controller;

import com.barbershop.model.User;
import com.barbershop.repository.UserRepository;
import com.barbershop.service.LineNotificationService;
import com.barbershop.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SystemSettingController {

    @Autowired
    private SystemSettingService systemSettingService;
    
    @Autowired
    private LineNotificationService lineNotificationService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, String>> getAllSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // Removed pending Security Config
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> settings) {
        systemSettingService.updateSettings(settings);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/notify-open-booking")
    public ResponseEntity<?> notifyOpenBooking(@RequestBody Map<String, Integer> payload) {
        Integer month = payload.get("month");
        if (month == null || month < 1 || month > 12) {
            return ResponseEntity.badRequest().body("Invalid month");
        }
        
        // Find all users with LINE ID
        List<User> users = userRepository.findByLineUserIdIsNotNull();
        
        // Send async or loop? Loop for now, but in production use async or task queue
        // Or Line Multicast
        for (User user : users) {
             try {
                lineNotificationService.sendBookingPeriodOpen(user, month);
             } catch (Exception e) {
                 // Log error but continue
                 e.printStackTrace();
             }
        }
        
        return ResponseEntity.ok().build();
    }
}
