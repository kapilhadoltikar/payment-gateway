package com.paymentgateway.fraud.repository;

import com.paymentgateway.fraud.model.ModelDisagreement;
import com.paymentgateway.fraud.model.ModelDisagreement.DisagreementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelDisagreementRepository extends JpaRepository<ModelDisagreement, Long> {

    long countByType(DisagreementType type);

    @Query("SELECT d.type, COUNT(d) FROM ModelDisagreement d GROUP BY d.type")
    List<Object[]> getDisagreementStats();
}
