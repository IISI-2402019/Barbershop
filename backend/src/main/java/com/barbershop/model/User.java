package com.barbershop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // lineUserId can be null for walk-in customers
    @Column(name = "line_user_id", unique = true)
    private String lineUserId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "real_name")
    private String realName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "reminder_cycle")
    private Integer reminderCycle; // In days. Nullable.

    public User() {
    }

    public User(String lineUserId, String displayName, UserRole role) {
        this.lineUserId = lineUserId;
        this.displayName = displayName;
        this.role = role;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLineUserId() {
        return lineUserId;
    }

    public void setLineUserId(String lineUserId) {
        this.lineUserId = lineUserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Integer getReminderCycle() {
        return reminderCycle;
    }

    public void setReminderCycle(Integer reminderCycle) {
        this.reminderCycle = reminderCycle;
    }
}
