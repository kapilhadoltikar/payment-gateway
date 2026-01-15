package com.paymentgateway.vault.repository;

import com.paymentgateway.vault.entity.CardData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardDataRepository extends JpaRepository<CardData, String> {
}
