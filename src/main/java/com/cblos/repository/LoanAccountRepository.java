package com.cblos.repository;

import com.cblos.model.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Integer> {

    Optional<LoanAccount> findByAccountNumber(String accountNumber);

    Optional<LoanAccount> findByLoanApplication_ApplicationId(Integer applicationId);
}