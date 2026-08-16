package com.cblos.service;

import com.cblos.model.CorporateCustomer;
import com.cblos.model.LoanOfficer;
import com.cblos.model.AppUser; 
import com.cblos.repository.CorporateCustomerRepository;
import com.cblos.repository.LoanOfficerRepository;
import com.cblos.repository.AppUserRepository;
import com.cblos.repository.CorporateContactRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cblos.dto.CustomerReviewResponse;
import com.cblos.dto.PrimaryContactResponse;
import com.cblos.model.CorporateContact;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private CorporateCustomerRepository customerRepository;

    @Autowired
    private LoanOfficerRepository officerRepository;

    @Autowired
    private AppUserRepository userRepository; 

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @Autowired
    private CorporateContactRepository contactRepository;

    //get all pending register application
    public List<CustomerReviewResponse> getPendingRegistrations() {
        return customerRepository.findAll().stream()
                .filter(customer -> "PENDING_VERIFICATION".equalsIgnoreCase(
                        customer.getStatus()))
                .map(this::toCustomerReviewResponse)
                .toList();
    }

    @Transactional 
    public AppUser reviewCustomerRegistration(Integer customerId, boolean approve, String reason) {
        CorporateCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Corporate profile not found with ID: " + customerId));    
        if (approve) {
            customer.setStatus("ACTIVE");
            customer.setRejectionReason(null);
            System.out.println(" [Admin Hub] Profile approved! Activated existing user credentials for: " + customer.getCompanyEmail());

        } else {
            customer.setStatus("REJECTED");
            customer.setRejectionReason(reason != null ? reason : "Legal documents could not be verified by the admin.");
        }
        customerRepository.save(customer);

        if (approve) {
            AppUser userCredential = userRepository.findByCorporateCustomerId(customerId)
                    .or(() -> userRepository.findByEmail(customer.getCompanyEmail()))
                    .orElseGet(AppUser::new);

            userCredential.setEmail(customer.getCompanyEmail());
            userCredential.setRole(com.cblos.model.UserRole.CUSTOMER);
            userCredential.setCorporateCustomerId(customer.getId());

            if (userCredential.getPassword() == null || userCredential.getPassword().isBlank()) {
                String storedPassword = customer.getTempRegistrationPassword();
                if (storedPassword == null || storedPassword.isBlank()) {
                    throw new IllegalStateException("Registration password missing for customer ID: " + customerId);
                }
                userCredential.setPassword(encodeIfNeeded(storedPassword));
            }

            customer.setTempRegistrationPassword(null);
            customerRepository.save(customer);
            return userRepository.save(userCredential);
        }

        return null;
    }

    @Transactional 
    public LoanOfficer onboardBankStaff(LoanOfficer newStaff, String employeeEmail) {
        if (newStaff.getEmployeeId() == null || newStaff.getEmployeeId().trim().isEmpty()) {
            throw new IllegalArgumentException("Validation Failed: Employee ID code cannot be blank.");
        }
        if (officerRepository.findByEmployeeId(newStaff.getEmployeeId()).isPresent()) {
            throw new IllegalStateException("Validation Failed: This employee ID is already assigned to another staff member.");
        }
        if (employeeEmail == null || employeeEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Validation Failed: Employee email cannot be blank.");
        }
        if (officerRepository.findByEmployeeEmail(employeeEmail).isPresent()) {
            throw new IllegalStateException("Validation Failed: This employee email is already assigned to another staff profile.");
        }
        if (userRepository.findByEmail(employeeEmail).isPresent()) {
            throw new IllegalStateException("Validation Failed: A security user with this email already exists.");
        }
        
        if (newStaff.getRole() == null) {
            newStaff.setRole("OFFICER"); 
        }
        
        newStaff.setEmployeeEmail(employeeEmail);
        newStaff.setActiveApplicationCount(0); 

        LoanOfficer savedStaff = officerRepository.save(newStaff);
        AppUser staffUser = new AppUser();
        staffUser.setEmail(employeeEmail); 

        String plainPassword = newStaff.getPassword();
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Validation Failed: You must specify an initial login password for this staff member.");
        }
        
        staffUser.setPassword(passwordEncoder.encode(plainPassword));

        if ("MANAGER".equalsIgnoreCase(savedStaff.getRole())) {
            staffUser.setRole(com.cblos.model.UserRole.MANAGER);
        } else {
            staffUser.setRole(com.cblos.model.UserRole.OFFICER);
        }
        
        staffUser.setLoanOfficerId(savedStaff.getId()); 

        userRepository.save(staffUser);
        System.out.println("[Security Vault] Dynamic login generated for " + savedStaff.getRole() + ": " + employeeEmail);

        return savedStaff;
    }

    private String encodeIfNeeded(String password) {
        return isBCryptHash(password) ? password : passwordEncoder.encode(password);
    }

    private boolean isBCryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    private CustomerReviewResponse toCustomerReviewResponse(
            CorporateCustomer customer) {

        CorporateContact contact = contactRepository
                .findByCorporateCustomerIdAndPrimaryTrue(
                        customer.getId())
                .orElse(null);

        PrimaryContactResponse contactResponse = null;

        if (contact != null) {
            contactResponse = new PrimaryContactResponse(
                    contact.getId(),
                    contact.getFirstName(),
                    contact.getLastName(),
                    contact.getEmail(),
                    contact.getPhoneNumber(),
                    contact.getDesignation());
        }

        return new CustomerReviewResponse(
                customer.getId(),
                customer.getCompanyName(),
                customer.getTaxId(),
                customer.getCompanyEmail(),
                customer.getPhoneNumber(),
                customer.getBusinessAddress(),
                customer.getIndustryType(),
                customer.getStatus(),
                customer.getRejectionReason(),
                contactResponse);
    }
}
