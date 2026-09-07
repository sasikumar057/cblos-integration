package com.cblos.repository;

import org.springframework.data.jpa.repository.JpaRepository;



import com.cblos.model.LoanApplication;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Integer> {

    java.util.List<LoanApplication> findByCustomer_Id(Integer customerId);
    
    java.util.List<LoanApplication> findByLoanOfficer_Id(Integer loanOfficerId);

    
}
