package com.cblos.controller;

import com.cblos.model.LoanOfficer;
import com.cblos.service.LoanOfficerService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/officers")
public class LoanOfficerController {

    @Autowired
    private LoanOfficerService officerService;

    // Register a new loan officer
    @PostMapping("/register")
    public ResponseEntity<LoanOfficer> registerOfficer(@RequestBody LoanOfficer officer) {
        return ResponseEntity.ok(officerService.registerOfficer(officer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanOfficer> getOfficer(@PathVariable Integer id) {
        return ResponseEntity.ok(officerService.getOfficerById(id));
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<LoanOfficer>> getAllOfficer(){
    	return ResponseEntity.ok(officerService.getAllLoanOfficer());
    }
}