package com.cblos.controller;

import com.cblos.service.CreditAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credit")
public class CreditAssessmentController {

    @Autowired
    private CreditAssessmentService assessmentService;

    @GetMapping("/risk-score/{applicationId}")
    public ResponseEntity<Double> getRiskScore(@PathVariable Integer applicationId) {
        Double score = assessmentService.getRiskScore(applicationId);
        return ResponseEntity.ok(score);
    }
}