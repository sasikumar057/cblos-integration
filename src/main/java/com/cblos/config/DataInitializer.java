package com.cblos.config;

import com.cblos.model.AppUser;
import com.cblos.model.UserRole;
import com.cblos.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Stop execution if the system user vault already has data
        if (appUserRepository.count() > 0) {
            return;
        }

        // Encrypt the master root password using BCrypt matching your security config bean
        String encoded = passwordEncoder.encode("admin@CBLOS2026");

        // ─── 🔐 SEED ONLY THE MASTER SYSTEM ADMIN ───
        saveUser("admin@cblos.com", encoded, UserRole.ADMIN, null, null); 

        System.out.println("========================================================================");
        System.out.println("👑 SYSTEM INITIALIZATION: ROOT MASTER ADMIN SEEDED SUCCESSFULLY");
        System.out.println("   -> Login Email : admin@cblos.com");
        System.out.println("   -> Password    : admin@CBLOS2026");
        System.out.println("========================================================================");
    }

    private void saveUser(String email, String encodedPassword, UserRole role,
                          Integer corporateCustomerId, Integer loanOfficerId) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setCorporateCustomerId(corporateCustomerId);
        user.setLoanOfficerId(loanOfficerId);
//        user.setActive(true); // Born active so basic auth accepts it instantly
//        
        appUserRepository.save(user);
    }
}