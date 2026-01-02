package com.barbershop.controller;

import com.barbershop.dto.LoginRequest;
import com.barbershop.model.User;
import com.barbershop.model.UserRole;
import com.barbershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

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
    public ResponseEntity<?> completeProfile(@PathVariable Long id, @RequestBody java.util.Map<String, Object> payload) {
        String realName = (String) payload.get("realName");
        String phone = (String) payload.get("phone");

        if (realName == null || phone == null) {
            return ResponseEntity.badRequest().body("Missing fields");
        }

        // Taiwan phone validation: 09xxxxxxxx
        if (!phone.matches("^09\\d{8}$")) {
             return ResponseEntity.badRequest().body("Invalid phone number format. Must be 09xxxxxxxx");
        }

        return userRepository.findById(id).map(user -> {
            user.setRealName(realName);
            user.setPhone(phone);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }
}
