package com.cblos.controller;

import com.cblos.model.LoanApplication;
import com.cblos.service.LoanApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class LoanApplicationController {

    @Autowired
    private LoanApplicationService loanService;

    // 1. Submit a new application tied to a specific customer
    @PostMapping("/submit/{customerId}")
    public ResponseEntity<LoanApplication> submitApplication(
            @PathVariable Integer customerId, 
            @RequestBody LoanApplication application) {
        LoanApplication savedApplication = loanService.submitApplication(application, customerId);
        return ResponseEntity.ok(savedApplication);
    }

    // 2. Track status
    @GetMapping("/status/{id}")
    public ResponseEntity<String> trackApplicationStatus(@PathVariable Integer id) {
        return ResponseEntity.ok(loanService.getStatusById(id));
    }

    // 3. View full details
    @GetMapping("/{id}")
    public ResponseEntity<LoanApplication> viewApplicationDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(loanService.getApplicationById(id));
    }
    
    // 4. View all applications
    @GetMapping("/all")
    public ResponseEntity<List<LoanApplication>> getAllApplications() {
        return ResponseEntity.ok(loanService.getAllApplications());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanApplication>> getApplicationsByCustomer(@PathVariable Integer customerId) {
        List<LoanApplication> apps = loanService.getAllApplications().stream()
                .filter(app -> app.getCustomer() != null && app.getCustomer().getId().equals(customerId))
                .filter(app -> !"APPROVED".equalsIgnoreCase(app.getStatus()))
                .filter(app -> !"WITHDRAWN".equalsIgnoreCase(app.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(apps);
    }

    @PutMapping("/withdraw/{id}")
    public ResponseEntity<LoanApplication> withdrawApplication(@PathVariable Integer id) {
        return ResponseEntity.ok(loanService.withdrawApplication(id));
    }
    
    //🧑‍💼 LOAN OFFICER WORKFLOW: Evaluate application and pass it up
    // PUT http://localhost:8080/api/loans/officer-review/1?score=720&notes=Vetted&pass=true
    @PutMapping("/officer-review/{id}")
    public ResponseEntity<LoanApplication> officerReview(
            @PathVariable Integer id,
            @RequestParam("score") Integer score,
            @RequestParam("notes") String notes,
            @RequestParam("pass") boolean pass) {
        // Calling the evaluation brain we structured
        return ResponseEntity.ok(loanService.officerEvaluateApplication(id, score, notes, pass));
    }

    // 🏢 MANAGER WORKFLOW: Final financial sign-off approval
    // PUT http://localhost:8080/api/loans/manager-approval/1?approve=true
    @PutMapping("/manager-approval/{id}")
    public ResponseEntity<LoanApplication> managerApproval(
            @PathVariable Integer id,
            @RequestParam("approve") boolean approve) {
        return ResponseEntity.ok(loanService.managerFinalDecision(id, approve));
    }


    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<LoanApplication>> getMyWorkspace(@PathVariable Integer officerId) {
        List<LoanApplication> myLoans = loanService.getApplicationsByOfficer(officerId);
        return ResponseEntity.ok(myLoans);
    }

    @GetMapping("/manager/{managerId}")
public ResponseEntity<List<LoanApplication>> getLoansByManager(@PathVariable Integer managerId) {
    // Filter applications by manager id using the service (no repository required here)
    List<LoanApplication> loans = loanService.getApplicationsByOfficer(managerId);
    return ResponseEntity.ok(loans);
}
}
