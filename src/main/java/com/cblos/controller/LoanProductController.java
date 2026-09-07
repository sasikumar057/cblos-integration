package com.cblos.controller;

import com.cblos.model.LoanProduct;
import com.cblos.service.LoanProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class LoanProductController {

    @Autowired
    private LoanProductService productService;

    /**
     * ─── GET CREDITS PRODUCT CATALOG MENU ───
     * Target Endpoint Destination: GET http://localhost:8080/api/products/catalog
     * * Pulls down all 3 seeded financial choices to draw the frontend catalog selection screen.
     */
    @GetMapping("/catalog")
    public ResponseEntity<List<LoanProduct>> getActiveBankingCatalog() {
        List<LoanProduct> activeCatalog = productService.getAllAvailableProducts();
        return ResponseEntity.ok(activeCatalog);
    }
}