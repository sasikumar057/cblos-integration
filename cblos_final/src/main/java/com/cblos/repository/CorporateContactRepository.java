package com.cblos.repository;

import com.cblos.model.CorporateContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorporateContactRepository
        extends JpaRepository<CorporateContact, Integer> {

    Optional<CorporateContact> findByEmailIgnoreCase(String email);

    Optional<CorporateContact>
        findByCorporateCustomerIdAndEmailIgnoreCase(
            Integer corporateCustomerId,
            String email
        );

    Optional<CorporateContact>
        findByCorporateCustomerIdAndPrimaryTrue(
            Integer corporateCustomerId
        );
}