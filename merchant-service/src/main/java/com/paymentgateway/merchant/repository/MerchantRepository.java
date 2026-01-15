package com.paymentgateway.merchant.repository;

import com.paymentgateway.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByEmail(String email);

    Optional<Merchant> findByApiKey(String apiKey);
}
