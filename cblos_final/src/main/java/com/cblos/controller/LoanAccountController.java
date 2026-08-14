package com.cblos.controller;

import com.cblos.model.LoanAccount;
import com.cblos.repository.LoanAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class LoanAccountController {

    @Autowired
    private LoanAccountRepository accountRepository;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanAccount>> getAccountsByCustomer(@PathVariable Integer customerId) {
        List<LoanAccount> accounts = accountRepository.findAll().stream()
                .filter(acc -> acc.getCustomer() != null && acc.getCustomer().getId().equals(customerId))
                .filter(acc -> !"SETTLED_CLOSED".equalsIgnoreCase(acc.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<LoanAccount> getAccountById(@PathVariable Integer accountId) {
        return accountRepository.findById(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
