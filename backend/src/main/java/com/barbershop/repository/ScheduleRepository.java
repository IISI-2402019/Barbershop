package com.barbershop.repository;

import com.barbershop.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStylistId(Long stylistId);
    
    // Find overlapping schedules for a specific stylist OR global schedules (stylistId is null)
    // A schedule overlaps with [start, end] if:
    // (schedule.stylist.id = :stylistId OR schedule.stylist IS NULL) AND
    // schedule.startTime < end AND schedule.endTime > start
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Schedule s WHERE (s.stylist.id = :stylistId OR s.stylist IS NULL) AND s.startTime < :end AND s.endTime > :start")
    List<Schedule> findOverlappingSchedules(Long stylistId, LocalDateTime end, LocalDateTime start);
}
