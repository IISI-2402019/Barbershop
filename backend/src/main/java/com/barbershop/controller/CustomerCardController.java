package com.barbershop.controller;

import com.barbershop.model.CustomerCard;
import com.barbershop.model.User;
import com.barbershop.repository.CustomerCardRepository;
import com.barbershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer-cards")
public class CustomerCardController {
    @Autowired
    private CustomerCardRepository customerCardRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<CustomerCard> getAllCards() {
        return customerCardRepository.findAllByOrderByCardDateDesc();
    }

    @GetMapping("/user/{userId}")
    public List<CustomerCard> getCardsByUser(@PathVariable Long userId) {
        return customerCardRepository.findByUserIdOrderByCardDateDesc(userId);
    }

    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody Map<String, Object> payload) {
        Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
        if (userId == null) return ResponseEntity.badRequest().body("userId is required");

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        CustomerCard card = new CustomerCard();
        card.setUser(userOpt.get());
        card.setContent(payload.get("content") != null ? payload.get("content").toString() : "");
        card.setImages(payload.get("images") != null ? payload.get("images").toString() : "[]");
        
        String dateStr = payload.get("cardDate") != null ? payload.get("cardDate").toString() : null;
        card.setCardDate(dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now());

        CustomerCard saved = customerCardRepository.save(card);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCard(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<CustomerCard> existingOpt = customerCardRepository.findById(id);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        CustomerCard existing = existingOpt.get();
        existing.setContent(payload.get("content") != null ? payload.get("content").toString() : existing.getContent());
        existing.setImages(payload.get("images") != null ? payload.get("images").toString() : existing.getImages());
        
        if (payload.get("cardDate") != null) {
            existing.setCardDate(LocalDate.parse(payload.get("cardDate").toString()));
        }
        
        customerCardRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        customerCardRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
