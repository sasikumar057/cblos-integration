package com.cblos.controller;

import com.cblos.model.CorporateCustomer;
import com.cblos.model.LoanOfficer;
import com.cblos.repository.CorporateCustomerRepository;
import com.cblos.service.AdminService;
import com.cblos.service.LoanOfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private LoanOfficerService officerService;

    @Autowired
    private CorporateCustomerRepository customerRepository;

    //get all pending customer registered
    @GetMapping("/customers/pending")
    public ResponseEntity<List<CorporateCustomer>> listPendingCustomers() {
        return ResponseEntity.ok(adminService.getPendingRegistrations());
    }

    @PutMapping("/customers/review/{customerId}")
    public ResponseEntity<CorporateCustomer> reviewCustomer(
            @PathVariable Integer customerId,
            @RequestParam("approve") boolean approve,
            @RequestParam(value = "reason", required = false) String reason) {
        adminService.reviewCustomerRegistration(customerId, approve, reason);
        return customerRepository.findById(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //officer and manager onbaording
    @PostMapping("/staff/onboard")
    public ResponseEntity<LoanOfficer> onboardStaff(
            @RequestBody LoanOfficer staffDetails, 
            @RequestParam String employeeEmail) {
        
        LoanOfficer savedStaff = adminService.onboardBankStaff(staffDetails, employeeEmail);
        return new ResponseEntity<>(savedStaff, HttpStatus.CREATED);
    }

    @GetMapping("/staff/{id}")
    public ResponseEntity<LoanOfficer> getStaffMember(@PathVariable Integer id) {
        return ResponseEntity.ok(officerService.getOfficerById(id));
    }

    @GetMapping("/staff/all")
    public ResponseEntity<List<LoanOfficer>> getAllStaff() {
        return ResponseEntity.ok(officerService.getAllLoanOfficer());
    }
}