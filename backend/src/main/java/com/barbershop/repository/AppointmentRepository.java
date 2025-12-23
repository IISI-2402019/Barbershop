package com.barbershop.repository;

import com.barbershop.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // 查詢某位設計師在特定時間範圍內的預約 (用於檢查衝突)
    List<Appointment> findByStylistIdAndStartTimeBetween(Long stylistId, LocalDateTime start, LocalDateTime end);

    // 查詢某位顧客的預約紀錄 (依時間倒序)
    List<Appointment> findByCustomerIdOrderByStartTimeDesc(Long customerId);
    
    // 查詢某位設計師的所有預約 (用於排班表)
    List<Appointment> findByStylistIdOrderByStartTime(Long stylistId);
}
