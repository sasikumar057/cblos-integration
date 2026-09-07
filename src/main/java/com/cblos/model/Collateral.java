package com.cblos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "collateral")
public class Collateral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "collateral_type")
    private String collateralType; 

    @Column(name = "estimated_value", precision = 18, scale = 2)
    private BigDecimal estimatedValue;
    
    @Column(name = "asset_reference_number", nullable = false)
    private String assetReferenceNumber;

    @Column(length = 1000)
    private String description;

    public Collateral() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LoanApplication getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplication loanApplication) { this.loanApplication = loanApplication; }

    public String getCollateralType() { return collateralType; }
    public void setCollateralType(String collateralType) { this.collateralType = collateralType; }

    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(BigDecimal estimatedValue) { this.estimatedValue = estimatedValue; }
    
    public String getAssetReferenceNumber() { return assetReferenceNumber; }
    public void setAssetReferenceNumber(String assetReferenceNumber) { this.assetReferenceNumber = assetReferenceNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}