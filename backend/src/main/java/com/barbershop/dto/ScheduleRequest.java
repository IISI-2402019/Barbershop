package com.barbershop.dto;

import java.time.LocalDateTime;

public class ScheduleRequest {
    private Long stylistId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isAllDay;
    private String reason;

    public Long getStylistId() {
        return stylistId;
    }

    public void setStylistId(Long stylistId) {
        this.stylistId = stylistId;
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
