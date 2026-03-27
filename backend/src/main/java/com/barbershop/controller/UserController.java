package com.barbershop.controller;

import com.barbershop.dto.LoginRequest;
import com.barbershop.dto.LoginResponse;
import com.barbershop.util.JwtUtil;
import com.barbershop.model.Appointment;
import com.barbershop.model.CustomerCard;
import com.barbershop.model.User;
import com.barbershop.model.UserRole;
import com.barbershop.model.Stylist;
import com.barbershop.repository.AppointmentRepository;
import com.barbershop.repository.CustomerCardRepository;
import com.barbershop.repository.UserRepository;
import com.barbershop.repository.StylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StylistRepository stylistRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private CustomerCardRepository customerCardRepository;

    @Autowired
    private JwtUtil jwtUtil;

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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        System.out.println("Login request received for Line User ID: " + request.getLineUserId()); // Debug log
        Optional<User> existingUser = userRepository.findByLineUserId(request.getLineUserId());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update display name if changed
            if (request.getDisplayName() != null && !request.getDisplayName().equals(user.getDisplayName())) {
                user.setDisplayName(request.getDisplayName());
                userRepository.save(user);
            }
            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new LoginResponse(token, user));
        } else {
            // Register new user
            User newUser = new User(request.getLineUserId(), request.getDisplayName(), UserRole.CUSTOMER);
            User savedUser = userRepository.save(newUser);
            String token = jwtUtil.generateToken(savedUser);
            return ResponseEntity.ok(new LoginResponse(token, savedUser));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/complete-profile")
    @org.springframework.transaction.annotation.Transactional
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

        Optional<User> currentUserOpt = userRepository.findById(id);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User currentUser = currentUserOpt.get();

        // --- Account Merge Logic ---
        // Check if a guest account (no LINE ID) already exists with this phone number.
        // This handles the case where an admin manually added a walk-in customer, and
        // that customer later logs in via LINE for the first time.
        Optional<User> existingGuestOpt = userRepository.findByPhoneAndLineUserIdIsNull(phone);

        if (existingGuestOpt.isPresent() && !existingGuestOpt.get().getId().equals(currentUser.getId())) {
            // Found a guest account to merge into. Current user must be a LINE user (has lineUserId).
            User guestUser = existingGuestOpt.get();
            String currentLineUserId = currentUser.getLineUserId();
            String currentDisplayName = currentUser.getDisplayName();

            // 1. Bind LINE info to the existing guest account
            guestUser.setLineUserId(currentLineUserId);
            guestUser.setDisplayName(currentDisplayName);
            guestUser.setRealName(realName);
            guestUser.setPhone(phone);
            guestUser.setReminderCycle(finalReminderCycle);

            // 2. Re-link any appointments the new LINE user may have (should be none, but be safe)
            List<Appointment> lineUserAppointments = appointmentRepository.findByCustomer(currentUser);
            for (Appointment appt : lineUserAppointments) {
                appt.setCustomer(guestUser);
            }
            appointmentRepository.saveAll(lineUserAppointments);

            // 3. Re-link any customer cards
            List<CustomerCard> lineUserCards = customerCardRepository.findByUser(currentUser);
            for (CustomerCard card : lineUserCards) {
                card.setUser(guestUser);
            }
            customerCardRepository.saveAll(lineUserCards);

            // 4. Save the merged guest account and delete the temp LINE user
            // Clear lineUserId from the temp user first to avoid unique constraint violation
            currentUser.setLineUserId(null);
            userRepository.save(currentUser);
            userRepository.flush(); // Ensure the NULL is committed before setting it on guest

            userRepository.save(guestUser);
            userRepository.delete(currentUser);

            System.out.println("Account merged: LINE user (id=" + id + ") merged into guest user (id=" + guestUser.getId() + ")");
            return ResponseEntity.ok(guestUser);
        }

        // --- Normal update (no merge needed) ---
        currentUser.setRealName(realName);
        currentUser.setPhone(phone);
        currentUser.setReminderCycle(finalReminderCycle);
        userRepository.save(currentUser);
        return ResponseEntity.ok(currentUser);
    }
}
