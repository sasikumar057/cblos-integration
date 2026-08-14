package com.cblos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cblos.model.CreditAssessment;
import com.cblos.model.LoanApplication;
import com.cblos.repository.CreditAssessmentRepository;
import java.time.LocalDate;

@Service
public class CreditAssessmentService {

    @Autowired
    private CreditAssessmentRepository assessmentRepository;

    public CreditAssessment evaluateCredit(LoanApplication application, Integer inputCreditScore, String riskNotes) {
        CreditAssessment assessment = new CreditAssessment();
        assessment.setLoanApplication(application);
        assessment.setAssessmentDate(LocalDate.now());

        assessment.setCreditScore(inputCreditScore);

        double amount = (application.getLoanAmount() != null) ? application.getLoanAmount().doubleValue() : 0.0;
        double baseRisk = (amount > 500000) ? 7.5 : 3.2;

        double optimizedRisk = (inputCreditScore >= 700) ? (baseRisk * 0.6) : baseRisk;
        assessment.setRiskScore(optimizedRisk);
        return assessmentRepository.save(assessment);
    }

    public Double getRiskScore(Integer applicationId) {
        return assessmentRepository.findByLoanApplication_ApplicationId(applicationId)
                .map(CreditAssessment::getRiskScore)
                .orElse(0.0);
    }
}