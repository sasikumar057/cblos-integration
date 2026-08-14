package com.cblos.repository;

import com.cblos.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByCorporateCustomerId(Integer corporateCustomerId);
}