package com.barbershop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "customer_cards")
public class CustomerCard extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "images", columnDefinition = "TEXT")
    private String images; // JSON array string

    @Column(name = "card_date", nullable = false)
    private LocalDate cardDate;

    public CustomerCard() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public LocalDate getCardDate() { return cardDate; }
    public void setCardDate(LocalDate cardDate) { this.cardDate = cardDate; }
}
