package com.barbershop.repository;

import com.barbershop.model.Stylist;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StylistRepository extends JpaRepository<Stylist, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stylist s WHERE s.id = :id")
    Optional<Stylist> findByIdWithLock(@Param("id") Long id);
}
