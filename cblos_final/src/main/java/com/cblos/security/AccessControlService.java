package com.cblos.security;

import com.cblos.model.Document;
import com.cblos.model.LoanApplication;
import com.cblos.model.UserRole;
import com.cblos.repository.DocumentRepository;
import com.cblos.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    public AppUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserDetails details)) {
            throw new AccessDeniedException("Security Exception: Session context is not authenticated.");
        }
        return details;
    }

    public boolean isCustomer() {
        return currentUser().getRole() == UserRole.CUSTOMER;
    }
    
    public boolean isAdmin() {
        return currentUser().getRole() == UserRole.ADMIN;
    }

    public boolean isBankStaff() {
        UserRole role = currentUser().getRole();
        return role == UserRole.OFFICER || role == UserRole.MANAGER;
    }

    public void ensureCustomerIdMatches(Integer customerId) {
        if (isAdmin()) return;
        if (!isCustomer()) return;
        
        Integer ownId = currentUser().getCorporateCustomerId();
        if (ownId == null || !ownId.equals(customerId)) {
            throw new AccessDeniedException("Access Denied: You cannot execute transactions for external company profiles.");
        }
    }

    public void ensureCustomerOwnsCustomerRecord(Integer customerId) {
        ensureCustomerIdMatches(customerId);
    }

    public void ensureCustomerOwnsApplication(Integer applicationId) {
        if (!isCustomer()) return;
        
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Loan application data file missing."));

        Integer ownId = currentUser().getCorporateCustomerId();
        if (ownId == null || application.getCustomer() == null || !ownId.equals(application.getCustomer().getId())) {
            throw new AccessDeniedException("Access Denied: This portfolio belongs to another registrant.");
        }
    }

    public void ensureCustomerOwnsDocument(Integer documentId) {
        if (!isCustomer()) return;
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document entity missing."));
        ensureCustomerOwnsApplication(document.getLoanApplication().getApplicationId());
    }

    public Integer getCurrentOfficerId() {
        if (!isBankStaff()) {
            throw new AccessDeniedException("Access Denied: Restrictive internal processing authority level required.");
        }
        Integer officerId = currentUser().getLoanOfficerId();
        if (officerId == null) {
            throw new AccessDeniedException("Error: User session profile is not linked onto an active loan officer node.");
        }
        return officerId;
    }
}