package com.cblos.controller;

import com.cblos.service.DisbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/disbursement")
public class DisbursementController {

    @Autowired
    private DisbursementService disbursementService;

    @GetMapping("/report")
    public ResponseEntity<String> generateDisbursementReport() {
        return ResponseEntity.ok(disbursementService.generateDisbursementReport());
    }
}