package com.cblos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "disbursement")
public class Disbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; 

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "disbursed_amount", precision = 18, scale = 2)
    private BigDecimal disbursedAmount;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "reference_number", unique = true)
    private String referenceNumber; 

    public Disbursement() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LoanAccount getLoanAccount() { return loanAccount; }
    public void setLoanAccount(LoanAccount loanAccount) { this.loanAccount = loanAccount; }

    public BigDecimal getDisbursedAmount() { return disbursedAmount; }
    public void setDisbursedAmount(BigDecimal disbursedAmount) { this.disbursedAmount = disbursedAmount; }

    public LocalDate getDisbursementDate() { return disbursementDate; }
    public void setDisbursementDate(LocalDate disbursementDate) { this.disbursementDate = disbursementDate; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
}