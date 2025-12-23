package com.barbershop.config;

import com.barbershop.model.*;
import com.barbershop.repository.AppointmentRepository;
import com.barbershop.repository.ServiceRepository;
import com.barbershop.repository.StylistRepository;
import com.barbershop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(StylistRepository stylistRepository, 
                                   ServiceRepository serviceRepository, 
                                   UserRepository userRepository,
                                   AppointmentRepository appointmentRepository) {
        return args -> {
            // Initialize Stylists
            if (stylistRepository.count() == 0) {
                stylistRepository.save(new Stylist("Alice", "Cut & Color", "https://i.pravatar.cc/150?u=alice"));
                stylistRepository.save(new Stylist("Bob", "Perm & Treatment", "https://i.pravatar.cc/150?u=bob"));
                stylistRepository.save(new Stylist("Charlie", "Men's Cut", "https://i.pravatar.cc/150?u=charlie"));
                System.out.println("Default stylists initialized.");
            }

            // Initialize Services
            if (serviceRepository.count() == 0) {
                serviceRepository.save(new Service("Cut + Wash", 1.0, new BigDecimal("500")));
                serviceRepository.save(new Service("Scalp Treatment", 1.5, new BigDecimal("1200")));
                serviceRepository.save(new Service("Hair Dye", 2.0, new BigDecimal("2000")));
                serviceRepository.save(new Service("Perm", 3.0, new BigDecimal("3000")));
                System.out.println("Default services initialized.");
            }

            // Initialize Users (Admin)
            if (userRepository.count() == 0) {
                User admin = new User("U1234567890abcdef1234567890abcdef", "Admin User", UserRole.ADMIN);
                admin.setRealName("System Admin");
                userRepository.save(admin);
                System.out.println("Default admin user initialized.");
            }
            
            // Initialize Dummy Appointment (Optional)
            if (appointmentRepository.count() == 0 && userRepository.count() > 0 && stylistRepository.count() > 0 && serviceRepository.count() > 0) {
                User user = userRepository.findAll().get(0);
                Stylist stylist = stylistRepository.findAll().get(0);
                Service service = serviceRepository.findAll().get(0);
                
                LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
                LocalDateTime end = start.plusMinutes((long)(service.getDurationHours() * 60));
                
                appointmentRepository.save(new Appointment(user, stylist, service, start, end, AppointmentStatus.BOOKED));
                System.out.println("Default appointment initialized.");
            }
        };
    }
}
