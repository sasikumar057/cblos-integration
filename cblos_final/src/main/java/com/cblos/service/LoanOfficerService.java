package com.cblos.service;

import com.cblos.model.LoanOfficer;
import com.cblos.repository.LoanOfficerRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanOfficerService {

    @Autowired
    private LoanOfficerRepository officerRepository;

    public LoanOfficer registerOfficer(LoanOfficer officer) {
        if (officerRepository.findByEmployeeId(officer.getEmployeeId()).isPresent()) {
            throw new RuntimeException("Officer with this Employee ID already exists.");
        }
        return officerRepository.save(officer);
    }

    public LoanOfficer getOfficerById(Integer id) {
        return officerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan Officer not found"));
    }
    
    public List<LoanOfficer> getAllLoanOfficer(){
    	return officerRepository.findAll();
    }
}