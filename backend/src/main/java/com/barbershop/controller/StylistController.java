package com.barbershop.controller;

import com.barbershop.model.Stylist;
import com.barbershop.model.Appointment;
import com.barbershop.repository.StylistRepository;
import com.barbershop.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/stylists")
public class StylistController {

    @Autowired
    private StylistRepository stylistRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping
    public List<Stylist> getAllStylists() {
        return stylistRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Stylist> createStylist(@RequestBody Stylist stylist) {
        Stylist savedStylist = stylistRepository.save(stylist);
        return new ResponseEntity<>(savedStylist, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Stylist> updateStylist(@PathVariable Long id, @RequestBody Stylist stylistDetails) {
        return stylistRepository.findById(id)
                .map(stylist -> {
                    stylist.setName(stylistDetails.getName());
                    if (stylistDetails.getAvatarUrl() != null) {
                        stylist.setAvatarUrl(stylistDetails.getAvatarUrl());
                    }
                    
                    Stylist updatedStylist = stylistRepository.save(stylist);
                    return new ResponseEntity<>(updatedStylist, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStylist(@PathVariable Long id) {
        if (stylistRepository.existsById(id)) {
            // Delete associated appointments first to avoid foreign key constraint violation
            List<Appointment> appointments = appointmentRepository.findByStylistIdOrderByStartTime(id);
            appointmentRepository.deleteAll(appointments);

            stylistRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
