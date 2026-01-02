package com.barbershop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stylist_id", nullable = true)
    private Stylist stylist;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_all_day", nullable = false)
    private Boolean isAllDay = false;

    private String reason;

    public Schedule() {
    }

    public Schedule(Stylist stylist, LocalDateTime startTime, LocalDateTime endTime, Boolean isAllDay, String reason) {
        this.stylist = stylist;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAllDay = isAllDay;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Stylist getStylist() {
        return stylist;
    }

    public void setStylist(Stylist stylist) {
        this.stylist = stylist;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsAllDay() {
        return isAllDay;
    }

    public void setIsAllDay(Boolean isAllDay) {
        this.isAllDay = isAllDay;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
