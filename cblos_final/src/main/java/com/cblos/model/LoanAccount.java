package com.cblos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_account")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber; 

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private CorporateCustomer customer;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "principal_amount", precision = 18, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate")
    private Double interestRate;

    @Column(length = 20)
    private String status; 

    @Column(name = "opening_date")
    private LocalDate openingDate;

    public LoanAccount() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public CorporateCustomer getCustomer() { return customer; }
    public void setCustomer(CorporateCustomer customer) { this.customer = customer; }

    public LoanApplication getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplication loanApplication) { this.loanApplication = loanApplication; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getOpeningDate() { return openingDate; }
    public void setOpeningDate(LocalDate openingDate) { this.openingDate = openingDate; }
}