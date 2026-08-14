package com.cblos.webController;

import com.cblos.model.Collateral;
import com.cblos.model.CorporateCustomer;
import com.cblos.model.LoanAccount;
import com.cblos.model.LoanApplication;
import com.cblos.service.CollateralService;
import com.cblos.service.CorporateCustomerService;
import com.cblos.service.DocumentService;
import com.cblos.service.LoanApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer/dashboard")
public class CorporateDashboardViewController {

    @Autowired
    private CorporateCustomerService customerService;

    @Autowired
    private LoanApplicationService loanService;
    
    @Autowired
    private CollateralService collateralService;
    
    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private com.cblos.repository.LoanAccountRepository accountRepository;

    @GetMapping("/{customerId}")
    public String showCustomerDashboard(@PathVariable Integer customerId, Model model) {
        
        CorporateCustomer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            throw new RuntimeException("Portal Error: Corporate Customer profile not found.");
        }
        model.addAttribute("customer", customer);

        List<LoanApplication> allApps = loanService.getAllApplications().stream()
                .filter(app -> app.getCustomer() != null && app.getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());

        List<LoanApplication> activeApplications = allApps.stream()
                .filter(app -> !"APPROVED".equalsIgnoreCase(app.getStatus()))
                .collect(Collectors.toList());

        List<LoanAccount> approvedLoans = accountRepository.findAll().stream()
                .filter(acc -> acc.getCustomer() != null && acc.getCustomer().getId().equals(customerId))
                .filter(acc -> !"SETTLED_CLOSED".equalsIgnoreCase(acc.getStatus())) 
                .collect(Collectors.toList());

        model.addAttribute("activeApplications", activeApplications);
        model.addAttribute("approvedLoans", approvedLoans); 

        model.addAttribute("newLoan", new LoanApplication());
        
        return "customer-dashboard";
    }
    
    @GetMapping("/{customerId}/apply-form")
    public String showDedicatedApplyForm(@PathVariable Integer customerId, 
                                         @RequestParam("productType") String productType, 
                                         Model model) {
        
        CorporateCustomer customer = customerService.getCustomerById(customerId);
        model.addAttribute("customer", customer);
        model.addAttribute("productType", productType);
        model.addAttribute("customerId", customerId);
        
        LoanApplication newLoanForm = new LoanApplication();
        newLoanForm.setLoanType(productType);
       
        model.addAttribute("newLoan", newLoanForm);
        
        return "loan-apply-form";
    }

    @PostMapping("/{customerId}/apply")
    public String submitLoanForm(@PathVariable Integer customerId, 
                                 @ModelAttribute("newLoan") LoanApplication application,
                                 @RequestParam("productType") String productType, Model model) {
               application.setLoanType(productType);
               
               model.addAttribute("application", application);

               LoanApplication loanApplication = loanService.submitApplication(application,customerId);
        return "redirect:/customer/dashboard/application/" + loanApplication.getApplicationId() + "/collateral";
    }

    @GetMapping("/application/{applicationId}/collateral")
    public String showCollateralForm(@PathVariable Integer applicationId,
    								 @ModelAttribute("application") LoanApplication application,
    								 Model model) {
    	
    	System.out.println(application.getLoanType());
        LoanApplication app = loanService.getApplicationById(applicationId);
        
        model.addAttribute("applicationId", applicationId);
        model.addAttribute("customerId", app.getCustomer().getId());
       
        return "collateral-entry";
    }

    @PostMapping("/application/{applicationId}/collateral/save")
    public String saveCollateralDetails(@PathVariable Integer applicationId, 
                                        @ModelAttribute Collateral collateral) {

        
        LoanApplication app = loanService.getApplicationById(applicationId);
        Integer customerId = app.getCustomer().getId();
        
        collateralService.addCollateralToApplication(applicationId, collateral);
        
        return "redirect:/customer/dashboard/application/{applicationId}/document";
    }
    
    
    @GetMapping("/application/{applicationId}/document")
    public String showDocumentUploadForm(@PathVariable("applicationId") Integer applicationId, Model model) {
        
        LoanApplication application = loanService.getApplicationById(applicationId);
        if (application == null || application.getCustomer() == null) {
            throw new RuntimeException("Secure Routing Failure: Target loan application or associated customer is missing.");
        }
        
        model.addAttribute("applicationId", applicationId);
        model.addAttribute("customerId", application.getCustomer().getId());

        return "document-upload";
    }
    
    
    @PostMapping("/application/{applicationId}/document/upload")
    public String handleDocumentUpload(@PathVariable("applicationId") Integer applicationId,
                                       @RequestParam("documentType") String documentType,
                                       @RequestParam("documentFile") MultipartFile file,
                                       @RequestParam("customerId") Integer customerId) throws IOException {
        
        if (!file.isEmpty()) {
            
            LoanApplication application = loanService.getApplicationById(applicationId);
            if (application == null) {
                throw new RuntimeException("Underwriting Error: Target application package not found with ID: " + applicationId);
            }
            
          
            documentService.uploadDocument(
                application,                         
                documentType,                       
                file.getOriginalFilename(),          
                file.getContentType(),               
                file.getBytes()                     
            );
        }
        
        return "redirect:/customer/dashboard/" + customerId + "/application-complete";
    }

    @GetMapping("/{customerId}/application-complete")
    public String showApplicationSuccessPage(@PathVariable("customerId") Integer customerId, Model model) {
        model.addAttribute("customerId", customerId);
        return "application-success";
    }
}