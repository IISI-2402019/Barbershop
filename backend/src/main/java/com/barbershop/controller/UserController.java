package com.barbershop.controller;

import com.barbershop.dto.LoginRequest;
import com.barbershop.model.User;
import com.barbershop.model.UserRole;
import com.barbershop.model.Stylist;
import com.barbershop.repository.UserRepository;
import com.barbershop.repository.StylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StylistRepository stylistRepository;

    @GetMapping
    public ResponseEntity<List<User>> searchUsers(@RequestParam(required = false) String query,
            @RequestParam(required = false) String role) {
        UserRole userRole = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                userRole = UserRole.valueOf(role);
            } catch (IllegalArgumentException e) {
                // ignore invalid role params or handle as bad request
            }
        }

        if (query != null && !query.trim().isEmpty()) {
            if (userRole != null) {
                return ResponseEntity
                        .ok(userRepository.findByRealNameContainingIgnoreCaseAndRole(query.trim(), userRole));
            }
            return ResponseEntity.ok(userRepository.findByRealNameContainingIgnoreCase(query.trim()));
        } else if (userRole != null) {
            return ResponseEntity.ok(userRepository.findByRole(userRole));
        }

        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/{id}/card")
    public ResponseEntity<?> updateCustomerCard(@PathVariable Long id,
            @RequestBody java.util.Map<String, Object> payload) {
        return userRepository.findById(id).map(user -> {
            if (payload.containsKey("content")) {
                user.setCustomerCardContent((String) payload.get("content"));
            }
            if (payload.containsKey("images")) {
                try {
                    List<?> imagesRaw = (List<?>) payload.get("images");
                    // Simple serialization without full ObjectMapper object if needed, but we
                    // imported it.
                    // Let's us ObjectMapper for safety.
                    String json = new ObjectMapper().writeValueAsString(imagesRaw);
                    user.setCustomerCardImages(json);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String roleStr = payload.get("role");
        if (roleStr == null) {
            return ResponseEntity.badRequest().body("必須指定角色權限");
        }
        try {
            UserRole newRole = UserRole.valueOf(roleStr);
            return userRepository.findById(id).map(user -> {
                user.setRole(newRole);
                userRepository.save(user);

                // If promoted to STYLIST, ensure a Stylist record exists
                if (newRole == UserRole.STYLIST) {
                    if (stylistRepository.findByUserId(user.getId()).isEmpty()) {
                        Stylist newStylist = new Stylist();
                        newStylist.setName(user.getRealName() != null ? user.getRealName() : user.getDisplayName());
                        newStylist.setUser(user);
                        stylistRepository.save(newStylist);
                    }
                } else {
                    // If demoted from STYLIST, remove Stylist record if exists
                    stylistRepository.findByUserId(user.getId()).ifPresent(existingStylist -> {
                        try {
                            stylistRepository.delete(existingStylist);
                        } catch (Exception e) {
                            throw new RuntimeException(
                                    "Cannot remove Stylist role because this user has associated appointments/schedules. Please reassign or delete them first.");
                        }
                    });
                }

                return ResponseEntity.ok(user);
            }).orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("無效的角色權限");
        }
    }

    @PostMapping("/login")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        System.out.println("Login request received for Line User ID: " + request.getLineUserId()); // Debug log
        Optional<User> existingUser = userRepository.findByLineUserId(request.getLineUserId());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update display name if changed
            if (request.getDisplayName() != null && !request.getDisplayName().equals(user.getDisplayName())) {
                user.setDisplayName(request.getDisplayName());
                userRepository.save(user);
            }
            return ResponseEntity.ok(user);
        } else {
            // Register new user
            User newUser = new User(request.getLineUserId(), request.getDisplayName(), UserRole.CUSTOMER);
            User savedUser = userRepository.save(newUser);
            return ResponseEntity.ok(savedUser);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/complete-profile")
    public ResponseEntity<?> completeProfile(@PathVariable Long id,
            @RequestBody java.util.Map<String, Object> payload) {
        String realName = (String) payload.get("realName");
        String phone = (String) payload.get("phone");
        Integer reminderCycle = null;

        if (payload.containsKey("reminderCycle") && payload.get("reminderCycle") != null) {
            try {
                if (payload.get("reminderCycle") instanceof Integer) {
                    reminderCycle = (Integer) payload.get("reminderCycle");
                } else {
                    reminderCycle = Integer.parseInt(payload.get("reminderCycle").toString());
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        if (realName == null || phone == null) {
            return ResponseEntity.badRequest().body("缺少必要欄位");
        }

        // Taiwan phone validation: 09xxxxxxxx
        if (!phone.matches("^09\\d{8}$")) {
            return ResponseEntity.badRequest().body("手機號碼格式錯誤 (需為 09 開頭的 10 碼數字)");
        }

        Integer finalReminderCycle = reminderCycle;

        return userRepository.findById(id).map(user -> {
            user.setRealName(realName);
            user.setPhone(phone);
            user.setReminderCycle(finalReminderCycle);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }
}
