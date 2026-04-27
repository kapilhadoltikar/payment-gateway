package com.paymentgateway.vault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // Token

    @Column(nullable = false, length = 1000)
    private String encryptedPan;

    @Column(nullable = false)
    private String expiryDate;

    @Column(nullable = false)
    private String cardHolderName;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
