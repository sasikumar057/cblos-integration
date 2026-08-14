package com.cblos.service;

import com.cblos.model.LoanProduct;
import com.cblos.repository.LoanProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanProductService {

    @Autowired
    private LoanProductRepository productRepository;

    public List<LoanProduct> getAllAvailableProducts() {
        return productRepository.findAll();
    }

    public LoanProduct getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Selected Credit Product option not found in database catalog."));
    }

    @PostConstruct
    public void seedInitialLoanProducts() {
        seedProduct(
                "COMMERCIAL_TERM_LOAN",
                "Designed for massive infrastructure capital acquisitions, plant machinery procurement, or business site structural expansion.",
                1000000.00,
                50000000.00,
                12,
                60,
                9.25
        );
        seedProduct(
                "WORKING_CAPITAL_LINE",
                "Short-term operating credit asset to manage day-to-day transaction flows, raw materials ledger, and emergency employee payroll gaps.",
                200000.00,
                10000000.00,
                6,
                24,
                11.50
        );
        seedProduct(
                "COMMERCIAL_LINE_OF_CREDIT",
                "Flexible corporate safety net. Draw funds up to your limit at any time, repay them, and draw again. Interest is charged only on the exact amount used.",
                500000.00,
                25000000.00,
                12,
                36,
                10.75
        );
        seedProduct(
                "EQUIPMENT_FINANCE",
                "Asset-backed finance for machinery, delivery vehicles, technology upgrades, and production equipment without blocking working capital.",
                500000.00,
                30000000.00,
                12,
                72,
                9.75
        );
        seedProduct(
                "INVOICE_FINANCING",
                "Unlock cash against approved receivables and customer invoices while waiting for enterprise buyers to complete payment cycles.",
                100000.00,
                15000000.00,
                3,
                12,
                12.25
        );
        seedProduct(
                "TRADE_FINANCE_FACILITY",
                "Short-cycle funding for purchase orders, supplier payments, import/export transactions, and inventory movement tied to confirmed trade flows.",
                300000.00,
                20000000.00,
                3,
                18,
                10.25
        );
    }

    private void seedProduct(String productName, String description, Double minAmount, Double maxAmount,
                             Integer minTenure, Integer maxTenure, Double rate) {
        if (productRepository.findByProductName(productName).isPresent()) {
            return;
        }

        LoanProduct product = new LoanProduct();
        product.setProductName(productName);
        product.setDescription(description);
        product.setMinLoanAmount(minAmount);
        product.setMaxLoanAmount(maxAmount);
        product.setMinTenureMonths(minTenure);
        product.setMaxTenureMonths(maxTenure);
        product.setDefaultInterestRate(rate);
        productRepository.save(product);
    }
}
