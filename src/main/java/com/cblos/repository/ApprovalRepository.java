package com.cblos.repository;

import com.cblos.model.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Integer> {
   //find application by specific with id
    List<Approval> findByLoanApplication_ApplicationId(Integer applicationId);

    List<Approval> findByApprovedBy_Id(Integer officerId);
}