package com.cblos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_application")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer applicationId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private CorporateCustomer customer;

    @ManyToOne
    @JoinColumn(name = "officer_id")
    private LoanOfficer loanOfficer; 

    @ManyToOne
    @JoinColumn(name = "loan_product_id")
    private LoanProduct loanProduct; 

    private String loanType; 
    private BigDecimal loanAmount;
    private String status;
    private LocalDate submissionDate;

    @Column(name = "officer_credit_score")
    private Integer officerCreditScore;

    @Column(name = "officer_assessment_notes", length = 1000)
    private String officerAssessmentNotes;

    @Column(name = "requested_tenure_months")
    private Integer requestedTenureMonths; 

    public LoanApplication() {}

    public Integer getApplicationId() { return applicationId; }
    public void setApplicationId(Integer applicationId) { this.applicationId = applicationId; }

    public CorporateCustomer getCustomer() { return customer; }
    public void setCustomer(CorporateCustomer customer) { this.customer = customer; }

    public LoanOfficer getLoanOfficer() { return loanOfficer; }
    public void setLoanOfficer(LoanOfficer loanOfficer) { this.loanOfficer = loanOfficer; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }

    public LoanProduct getLoanProduct() { return loanProduct; }
    public void setLoanProduct(LoanProduct loanProduct) { this.loanProduct = loanProduct; }

    public Integer getRequestedTenureMonths() { return requestedTenureMonths; }
    public void setRequestedTenureMonths(Integer requestedTenureMonths) { this.requestedTenureMonths = requestedTenureMonths; }

    public Integer getOfficerCreditScore() { return officerCreditScore; }
    public void setOfficerCreditScore(Integer officerCreditScore) { this.officerCreditScore = officerCreditScore; }

    public String getOfficerAssessmentNotes() { return officerAssessmentNotes; }
    public void setOfficerAssessmentNotes(String officerAssessmentNotes) { this.officerAssessmentNotes = officerAssessmentNotes; }
}
