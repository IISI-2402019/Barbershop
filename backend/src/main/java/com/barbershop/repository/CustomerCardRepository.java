package com.barbershop.repository;

import com.barbershop.model.CustomerCard;
import com.barbershop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerCardRepository extends JpaRepository<CustomerCard, Long> {
    List<CustomerCard> findByUser(User user);
    List<CustomerCard> findByUserIdOrderByCardDateDesc(Long userId);
    List<CustomerCard> findAllByOrderByCardDateDesc();
}
