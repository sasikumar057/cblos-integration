package com.cblos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "credit_assessment")
public class CreditAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "credit_score")
    private Integer creditScore;

    @Column(name = "assessment_date")
    private LocalDate assessmentDate;

    public CreditAssessment() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LoanApplication getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplication loanApplication) { this.loanApplication = loanApplication; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }

    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }
}