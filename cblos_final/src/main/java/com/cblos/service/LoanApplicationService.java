package com.cblos.service;

import com.cblos.model.LoanApplication;
import com.cblos.model.LoanOfficer;
import com.cblos.model.CorporateCustomer;
import com.cblos.model.LoanAccount;
import com.cblos.model.LoanProduct;
import com.cblos.repository.LoanApplicationRepository;
import com.cblos.repository.LoanOfficerRepository;
import com.cblos.repository.CorporateCustomerRepository;
import com.cblos.repository.DocumentRepository;
import com.cblos.repository.LoanAccountRepository;
import com.cblos.repository.LoanProductRepository;
import com.cblos.security.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LoanApplicationService {

    @Autowired
    private LoanApplicationRepository loanRepository;

    @Autowired
    private CorporateCustomerRepository customerRepository;
    
    @Autowired
    private LoanOfficerRepository officerRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private LoanProductRepository productRepository;

    @Autowired
    private AccessControlService accessControl;
    
    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private DisbursementService disbursementService;

    @Autowired
    private DocumentService documentService;

	    public LoanApplication submitApplication(LoanApplication app, Integer customerId) {
	        accessControl.ensureCustomerIdMatches(customerId);

	        CorporateCustomer customer = customerRepository.findById(customerId)
	                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
	        app.setCustomer(customer);
	        app.setSubmissionDate(LocalDate.now()); 
            app.setStatus("DOCUMENT_PENDING");
            productRepository.findByProductName(app.getLoanType())
                    .orElseThrow(() -> new IllegalArgumentException("Validation Failed: You must select a valid credit product from the catalog."));
	        
	        LoanProduct lp = new LoanProduct();
	        
	        Optional<LoanProduct> name = productRepository.findByProductName(app.getLoanType());
	        
	        if(name.isPresent()) {
	        	lp = name.get();
	        }
	        else {
	        	lp = name.get();
	        }
	        
	        app.setLoanProduct(lp);
	        if ("COMMERCIAL_TERM_LOAN".equalsIgnoreCase(app.getLoanType())) {
	            app.setLoanProduct(lp);
	        } else if ("WORKING_CAPITAL_LINE".equalsIgnoreCase(app.getLoanType())) {
	            app.setLoanProduct(lp);
	        } else if ("COMMERCIAL_LINE_OF_CREDIT".equalsIgnoreCase(app.getLoanType())) {
	            app.setLoanProduct(lp);
	        } else {
	            app.setLoanProduct(lp); 
	        }
	        
	        if (app.getLoanProduct() == null || app.getLoanProduct().getId() == null) {
	            throw new IllegalArgumentException("Validation Failed: You must select a valid credit product from the catalog.");
	        }
	
	        LoanProduct productRules = productRepository.findById(app.getLoanProduct().getId())
	                .orElseThrow(() -> new RuntimeException("Selected Credit Product option not found in database."));

	        app.setLoanType(productRules.getProductName());
	
	        BigDecimal amount = app.getLoanAmount();
	        Integer tenure = app.getRequestedTenureMonths();
	
	        if (amount == null || tenure == null) {
	            throw new IllegalArgumentException("Validation Failed: Requested loan amount and tenure timeline cannot be blank.");
	        }
	
	        if (tenure < productRules.getMinTenureMonths() || tenure > productRules.getMaxTenureMonths()) {
	            throw new IllegalArgumentException("Validation Failed: For " + productRules.getProductName() 
	                    + ", selected payback window must be between " + productRules.getMinTenureMonths() 
	                    + " and " + productRules.getMaxTenureMonths() + " months.");
	        }

	        BigDecimal minLimit = BigDecimal.valueOf(productRules.getMinLoanAmount());
	        BigDecimal maxLimit = BigDecimal.valueOf(productRules.getMaxLoanAmount());
	
	        if (amount.compareTo(minLimit) < 0 || amount.compareTo(maxLimit) > 0) {
	            throw new IllegalArgumentException("Validation Failed: For " + productRules.getProductName() 
	                    + ", requested amount must fall between ₹" + productRules.getMinLoanAmount() 
	                    + " and ₹" + productRules.getMaxLoanAmount());
	        }

	        if (tenure > 36) {
	            BigDecimal restrictedMaxLimit = maxLimit.multiply(BigDecimal.valueOf(0.70)); // Cuts exposure ceiling by 30%
	            if (amount.compareTo(restrictedMaxLimit) > 0) {
	                throw new IllegalArgumentException("Risk Guardrail: For extended terms exceeding 36 months, the maximum capital exposure allowed for this product is scaled down to ₹" + restrictedMaxLimit);
	            }
	        }
	        return loanRepository.save(app);
	    }

    public String getStatusById(Integer id) {
        accessControl.ensureCustomerOwnsApplication(id);
        documentService.reconcileLoanDocumentPackage(id);
        return loanRepository.findById(id)
                .map(LoanApplication::getStatus)
                .orElse("Application Not Found");
    }

    public LoanApplication getApplicationById(Integer id) {
        accessControl.ensureCustomerOwnsApplication(id);
        documentService.reconcileLoanDocumentPackage(id);
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan Application Not Found"));
    }
    
    public List<LoanApplication> getAllApplications() {
        if (accessControl.isCustomer()) {
            Integer companyId = accessControl.currentUser().getCorporateCustomerId();
            List<LoanApplication> applications = loanRepository.findByCustomer_Id(companyId);
            reconcileIncompleteDocumentPackages(applications);
            return applications; 
        }
        List<LoanApplication> applications = loanRepository.findAll();
        reconcileIncompleteDocumentPackages(applications);
        return applications;
    }

    private void reconcileIncompleteDocumentPackages(List<LoanApplication> applications) {
        for (LoanApplication application : applications) {
            String status = application.getStatus() == null ? "" : application.getStatus().trim().toUpperCase();
            boolean completePackage = hasCompleteDocumentPackage(application.getApplicationId());
            if (Set.of("UNDER_REVIEW", "PENDING", "PENDING_MANAGER_APPROVAL").contains(status)
                    && !completePackage) {
                LoanOfficer assignedOfficer = application.getLoanOfficer();
                if (assignedOfficer != null && assignedOfficer.getActiveApplicationCount() > 0) {
                    assignedOfficer.setActiveApplicationCount(assignedOfficer.getActiveApplicationCount() - 1);
                    officerRepository.save(assignedOfficer);
                }
                application.setLoanOfficer(null);
                application.setStatus("DOCUMENT_PENDING");
                loanRepository.save(application);
            } else if (completePackage && Set.of("DOCUMENT_PENDING", "PENDING").contains(status)) {
                routeCompleteDocumentPackage(application);
            }
        }
    }

    private void routeCompleteDocumentPackage(LoanApplication application) {
        if (application != null) {
            application.setLoanOfficer(null);
            application.setStatus("UNDER_REVIEW");
            loanRepository.save(application);
            return;
        }

        if (application.getLoanOfficer() != null) {
            application.setStatus("UNDER_REVIEW");
            loanRepository.save(application);
            return;
        }

        LoanOfficer availableOfficer = officerRepository.findLeastLoadedOfficer().orElse(null);
        if (availableOfficer == null) {
            application.setStatus("PENDING");
            loanRepository.save(application);
            return;
        }

        application.setLoanOfficer(availableOfficer);
        application.setStatus("UNDER_REVIEW");
        availableOfficer.setActiveApplicationCount(availableOfficer.getActiveApplicationCount() + 1);
        officerRepository.save(availableOfficer);
        loanRepository.save(application);
    }

    private boolean hasCompleteDocumentPackage(Integer applicationId) {
        if (applicationId == null) return false;
        Set<String> uploadedTypes = documentRepository.findByLoanApplicationApplicationId(applicationId).stream()
                .map(document -> document.getDocumentType())
                .filter(type -> type != null)
                .map(type -> type.trim().toUpperCase())
                .collect(Collectors.toSet());

        return uploadedTypes.contains("COLLATERAL_PROOF")
                && uploadedTypes.contains("TAX_RETURN")
                && uploadedTypes.contains("BUSINESS_LICENSE");
    }

    @Transactional
    public LoanApplication withdrawApplication(Integer applicationId) {
        if (!accessControl.isCustomer()) {
            throw new AccessDeniedException("Only customers can withdraw their own applications.");
        }
        accessControl.ensureCustomerOwnsApplication(applicationId);

        LoanApplication app = loanRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Loan Application Not Found with ID: " + applicationId));

        String status = app.getStatus() == null ? "" : app.getStatus().trim().toUpperCase();
        if (Set.of("APPROVED", "WITHDRAWN").contains(status)) {
            throw new IllegalStateException("This application is already closed and cannot be withdrawn.");
        }

        LoanOfficer assignedOfficer = app.getLoanOfficer();
        if (assignedOfficer != null && assignedOfficer.getActiveApplicationCount() > 0) {
            assignedOfficer.setActiveApplicationCount(assignedOfficer.getActiveApplicationCount() - 1);
            officerRepository.save(assignedOfficer);
        }

        app.setLoanOfficer(null);
        app.setStatus("WITHDRAWN");
        return loanRepository.save(app);
    }
    
    
    
@Transactional 
public LoanApplication officerEvaluateApplication(Integer applicationId, Integer creditScore, String assessmentNotes, boolean passToManager) {

    LoanApplication app = loanRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Loan Application Not Found with ID: " + applicationId));

    if (!"UNDER_REVIEW".equalsIgnoreCase(app.getStatus())) {
        throw new IllegalStateException("Operation Denied: This application is not currently under active officer review.");
    }

    LoanOfficer reviewingOfficer = officerRepository.findById(accessControl.getCurrentOfficerId())
            .orElseThrow(() -> new RuntimeException("Reviewing officer profile not found."));

    app.setLoanOfficer(reviewingOfficer);
    app.setOfficerCreditScore(creditScore);
    app.setOfficerAssessmentNotes(assessmentNotes);

    if (creditScore < 600) {
        app.setStatus("REJECTED");

        if (reviewingOfficer.getActiveApplicationCount() > 0) {
            reviewingOfficer.setActiveApplicationCount(reviewingOfficer.getActiveApplicationCount() - 1);
            officerRepository.save(reviewingOfficer);
        }
        
        System.out.println("Risk Engine Alert: Application automatically rejected due to poor credit score: " + creditScore);
        return loanRepository.save(app);
    }

    if (passToManager) {
    app.setStatus("PENDING_MANAGER_APPROVAL"); 

    if (reviewingOfficer.getActiveApplicationCount() > 0) {
        reviewingOfficer.setActiveApplicationCount(reviewingOfficer.getActiveApplicationCount() - 1);
        officerRepository.save(reviewingOfficer);
    }

    Optional<LoanOfficer> automaticManager = officerRepository.findLeastLoadedManager();
    
    if (automaticManager.isPresent()) {
        LoanOfficer manager = automaticManager.get();
        
        app.setLoanOfficer(manager); 

        manager.setActiveApplicationCount(manager.getActiveApplicationCount() + 1);
        officerRepository.save(manager);
        
        System.out.println("Assigned to Manager: " + manager.getName());
    } else {
       
        app.setLoanOfficer(null); 
    }
}
    return loanRepository.save(app);
}

    public LoanApplication managerFinalDecision(Integer applicationId, boolean finalApprove) {
        
        LoanApplication app = loanRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Loan Application Not Found with ID: " + applicationId));

        if (!"PENDING_MANAGER_APPROVAL".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException("Operation Denied: This application requires an Officer's recommendation before a Manager can act.");
        }

        if (finalApprove) {
            app.setStatus("APPROVED"); 
            System.out.println("Model State Updated: Application ID " + applicationId + " is officially APPROVED.");

            LoanAccount newAccount = new LoanAccount();
            newAccount.setLoanApplication(app);
            newAccount.setCustomer(app.getCustomer());
            newAccount.setPrincipalAmount(app.getLoanAmount());
            newAccount.setOpeningDate(LocalDate.now());
            newAccount.setStatus("Active");

            String systemGeneratedNo = "ACT-" + System.currentTimeMillis();
            newAccount.setAccountNumber(systemGeneratedNo);

            Integer requestedTenure = app.getRequestedTenureMonths();
            Double calculatedRate;

            if (requestedTenure == null || requestedTenure <= 12) {
                calculatedRate = 10.50; 
            } else if (requestedTenure <= 36) {
                calculatedRate = 12.00; 
            } else {
                calculatedRate = 14.50; 
            }

            newAccount.setInterestRate(calculatedRate); 
            System.out.println("Dynamic Pricing Applied: Tenure is " + requestedTenure + " months. Rate set to " + calculatedRate + "%");

            LoanAccount savedAccount = loanAccountRepository.save(newAccount);
            System.out.println(" Core Ledger Updated: Loan Account " + systemGeneratedNo + " generated.");

            disbursementService.scheduleDisbursement(savedAccount);

        } else {
            app.setStatus("REJECTED");
        }

        LoanOfficer assignedOfficer = app.getLoanOfficer();
        if (assignedOfficer != null && assignedOfficer.getActiveApplicationCount() > 0) {
            assignedOfficer.setActiveApplicationCount(assignedOfficer.getActiveApplicationCount() - 1);
            officerRepository.save(assignedOfficer);
        }

        return loanRepository.save(app);
    }

    @Transactional(readOnly = true)
    public List<LoanApplication> getApplicationsByOfficer(Integer officerId) {
        return loanRepository.findByLoanOfficer_Id(officerId);
    }

}
