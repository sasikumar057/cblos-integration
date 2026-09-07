package com.cblos.webController;

import com.cblos.model.LoanApplication;
import com.cblos.model.LoanOfficer;
import com.cblos.repository.DocumentRepository;
import com.cblos.repository.LoanOfficerRepository;
import com.cblos.model.Document;
import com.cblos.service.LoanApplicationService;
import com.cblos.service.LoanOfficerService;
import com.cblos.service.ApprovalService;
import com.cblos.service.DisbursementService;
import com.cblos.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager/dashboard/")
public class CorporateManagerViewController {

    @Autowired
    private LoanApplicationService loanService;

    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private LoanOfficerService loanofService;
    
    @Autowired
    private DisbursementService disbursement;
    
    @Autowired
    private ApprovalService approvalService;
    
    @Autowired
    private LoanOfficerRepository officerRepository;
    
    @Autowired
    private DocumentRepository documentRepository;

    @GetMapping("/workdesk/{managerId}")
    public String showManagerDashboard(@PathVariable("managerId") Integer managerId,
                                       @RequestParam(value = "selectedAppId", required = false) Integer selectedAppId,
                                       Model model) {
        
        LoanOfficer manager = loanofService.getOfficerById(managerId);
        
        if (manager == null) {
            throw new RuntimeException("Security Error: Manager profile not found with ID: " + managerId);
        }
        
        if (manager.getRole() == null || !"MANAGER".equalsIgnoreCase(manager.getRole().trim())) {
           
            throw new RuntimeException("Access Denied: User ID " + managerId + " does not hold managerial authorization authority level.");

        }
        
        List<LoanApplication> allApplications = loanService.getAllApplications();

        List<LoanApplication> managerQueue = allApplications.stream()
                .filter(app -> "PENDING".equalsIgnoreCase(app.getStatus()))
                .collect(Collectors.toList());

        LoanApplication selectedApp = null;
        if (selectedAppId != null) {
            selectedApp = managerQueue.stream()
                    .filter(app -> app.getApplicationId().equals(selectedAppId))
                    .findFirst()
                    .orElse(null);
        } else if (!managerQueue.isEmpty()) {
            selectedApp = managerQueue.get(0); 
        }

        if (selectedApp != null) {
            model.addAttribute("selectedApp", selectedApp);
            List<Document> loanDocuments = documentService.getDocumentsByLoan(selectedApp.getApplicationId());
            
            if (loanDocuments != null && !loanDocuments.isEmpty()) {
                model.addAttribute("activeDoc", loanDocuments.get(0));
            } else {
                model.addAttribute("activeDoc", null);
            }

            double loanAmt = selectedApp.getLoanAmount().doubleValue();
            double sampleCollateralValue = loanAmt * 1.5; 
            int margin = (int) ((sampleCollateralValue / loanAmt) * 100);
            
            model.addAttribute("collateralValue", sampleCollateralValue);
            model.addAttribute("coverageMargin", margin);
            model.addAttribute("marginPassed", margin >= 120);
        }

        model.addAttribute("managerQueue", managerQueue);
        model.addAttribute("pendingCount", managerQueue.size());
        model.addAttribute("managerName", manager.getName()); 
        model.addAttribute("managerId", managerId);          

        return "manager-dashboard";
    }


    @PostMapping("/authorize")
    public String authorizeApplication(@RequestParam("applicationId") Integer applicationId,
                                       @RequestParam("managerId") Integer managerId, 
                                       @RequestParam("managerNotes") String managerNotes,
                                       @RequestParam("actionOutcome") String actionOutcome) {
        
        try {
            if ("APPROVE".equalsIgnoreCase(actionOutcome)) {
               
                approvalService.processApproval(applicationId, "Approved", managerNotes);
                System.out.println("🏛️ [Manager Desk] Capital Disbursed & Account Initialized for App #" + applicationId);
                
            } else if ("REJECT".equalsIgnoreCase(actionOutcome)) {
                approvalService.submitApproval(applicationId, managerId, "MANAGER", "Rejected", managerNotes);
                System.out.println("❌ [Manager Desk] Application Refused & Closed for App #" + applicationId);
            }

            LoanOfficer manager = loanofService.getOfficerById(managerId);
            if (manager != null && manager.getActiveApplicationCount() > 0) {
                manager.setActiveApplicationCount(manager.getActiveApplicationCount() - 1);
            }

        } catch (Exception e) {
            System.err.println("❌ [Manager Underwriting Failure] Security or Validation Breach: " + e.getMessage());
            
            return "redirect:/customer/dashboard/manager/workdesk/" + managerId + "?error=" + e.getMessage();
        }

        return "redirect:/customer/dashboard/manager/workdesk/" + managerId;
    }
    
    
    
    @GetMapping("document/view/{id}")
    @ResponseBody 
    public ResponseEntity<byte[]> viewDocumentInline(@PathVariable("id") Integer id) {

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
        
        byte[] fileBytes = doc.getFileData();
        if (fileBytes == null || fileBytes.length == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(doc.getFileType())); 

        ContentDisposition contentDisposition = ContentDisposition.inline()
                .filename(doc.getFileName(), StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(contentDisposition);
        
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
}