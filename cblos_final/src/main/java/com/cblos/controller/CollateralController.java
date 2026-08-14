package com.cblos.controller;

import com.cblos.model.Collateral;
import com.cblos.service.CollateralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collateral")
public class CollateralController {

    @Autowired
    private CollateralService collateralService;

    // Add collateral to a specific application
    @PostMapping("/add/{applicationId}")
    public ResponseEntity<Collateral> addCollateral(
            @PathVariable Integer applicationId, 
            @RequestBody Collateral collateral) {
        return ResponseEntity.ok(collateralService.addCollateralToApplication(applicationId, collateral));
    }

    // View all collateral for an application
    @GetMapping("/loan/{applicationId}")
    public ResponseEntity<List<Collateral>> getCollateral(@PathVariable Integer applicationId) {
        return ResponseEntity.ok(collateralService.getCollateralForApplication(applicationId));
    }
}