package com.cblos.repository;

import com.cblos.model.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Integer> {
    List<RepaymentSchedule> findByLoanAccount_Id(Integer accountId);
}