package com.cblos.controller;

import com.cblos.dto.RegistrationStatusResponse;
import com.cblos.dto.CustomerPasswordResetRequest;
import com.cblos.model.CorporateCustomer;
import com.cblos.service.CorporateCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cblos.dto.CorporateRegistrationRequest;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class CorporateCustomerController {

    @Autowired
    private CorporateCustomerService customerService;

    //on board customer
    @PostMapping("/onboard")
    public ResponseEntity<CorporateCustomer> onboardCustomer(@RequestBody CorporateRegistrationRequest request) {
        CorporateCustomer createdCustomer = customerService.onboardCustomer(request);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    //check the status of registerd customer
    @GetMapping("/registration-status")
    public ResponseEntity<RegistrationStatusResponse> getRegistrationStatus(@RequestParam("email") String email) {
        return ResponseEntity.ok(customerService.getRegistrationStatusByEmail(email));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> resetCustomerPassword(@RequestBody CustomerPasswordResetRequest request) {
        customerService.resetCustomerPassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("Customer password updated successfully.");
    }

    @PutMapping("/verify/{id}")
    public ResponseEntity<CorporateCustomer> verifyCustomer(
            @PathVariable Integer id,
            @RequestParam("action") String action,
            @RequestParam(value = "reason", required = false) String reason) { 

        CorporateCustomer verifiedCustomer = customerService.verifyCustomerLegitimacy(id, action, reason);
        return ResponseEntity.ok(verifiedCustomer);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<CorporateCustomer> updateCustomerDetails(
            @PathVariable Integer id,
            @RequestBody CorporateCustomer partialUpdatedData) {
        
        CorporateCustomer editedCustomer = customerService.updatePendingCustomerDetails(id, partialUpdatedData);
        return ResponseEntity.ok(editedCustomer);
    }

    //Get specific customer profile
    @GetMapping("/{id}")
    public ResponseEntity<CorporateCustomer> getCustomer(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    //Get all customers
    @GetMapping("/all")
    public ResponseEntity<List<CorporateCustomer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}
