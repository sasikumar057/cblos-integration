package com.cblos.repository;

import com.cblos.model.CorporateCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CorporateCustomerRepository extends JpaRepository<CorporateCustomer, Integer> {
    Optional<CorporateCustomer> findByTaxId(String taxId);
    Optional<CorporateCustomer> findByCompanyEmailIgnoreCase(String companyEmail);
}
