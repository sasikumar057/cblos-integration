package com.cblos.webController;

import com.cblos.model.CorporateCustomer;
import com.cblos.service.CorporateCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
public class CorporateRegistrationViewController {

    @Autowired
    private CorporateCustomerService customerService;

    // 1. Render the blank registration page
    @GetMapping
    public String showRegistrationForm() {
        return "corporate-register";
    }

    // 2. Process form submission
    // Spring Boot automatically maps form inputs with matching 'name' attributes into the entity
    @PostMapping
    public String processRegistration(CorporateCustomer customer) {
        // Your existing service layer handles saving and setting default status to 'PENDING_VERIFICATION'
        customerService.onboardCustomer(customer);
        
        // Redirect to a clean success informational window
        return "redirect:/register/success";
    }

    @GetMapping("/success")
    public String showSuccessPage() {
        return "registration-success";
    }
}