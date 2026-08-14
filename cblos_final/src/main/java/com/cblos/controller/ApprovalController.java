package com.cblos.controller;

import com.cblos.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @PostMapping("/approve/{applicationId}")
    public ResponseEntity<String> approveLoan(
            @PathVariable Integer applicationId,
            @RequestParam(defaultValue = "Verified financial documents and collateral.") String comments) {
        approvalService.processApproval(applicationId, "Approved", comments);
        return ResponseEntity.ok(
                "Loan approved successfully. Loan Account and Disbursement Schedule have been generated.");
    }

    @PostMapping("/reject/{applicationId}")
    public ResponseEntity<String> rejectLoan(
            @PathVariable Integer applicationId,
            @RequestParam(defaultValue = "Failed credit assessment.") String comments) {
        approvalService.processApproval(applicationId, "Rejected", comments);
        return ResponseEntity.ok("Loan application has been rejected.");
    }
}