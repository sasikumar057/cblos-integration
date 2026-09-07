package com.cblos.webController;

import com.cblos.model.Document;
import com.cblos.model.LoanApplication;
import com.cblos.model.LoanOfficer;
import com.cblos.repository.DocumentRepository;
import com.cblos.repository.LoanOfficerRepository;
import com.cblos.service.CreditAssessmentService;
import com.cblos.service.DocumentService;
import com.cblos.service.LoanApplicationService;
import com.cblos.service.LoanOfficerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.ResponseBody;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/officer/dashboard/")
public class CorporateOfficerViewController {

    @Autowired
    private LoanApplicationService loanService;

    @Autowired
    private LoanOfficerService loanofService;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private LoanOfficerRepository officerRepository;
    
    @Autowired
    private CreditAssessmentService creditAssessmentService;
    

    @Autowired
    private DocumentService documentService; 

    @GetMapping("/workdesk/{officerId}")
    public String showOfficerDashboard(@PathVariable("officerId") Integer officerId, 
                                       @RequestParam(value = "selectedAppId", required = false) Integer selectedAppId,
                                       Model model) {
        
        LoanOfficer loanof = loanofService.getOfficerById(officerId);
        if (loanof == null) {
            throw new RuntimeException("Security Error: Underwriting Officer profile not found.");
        }
        
        if (loanof.getRole() == null || !"OFFICER".equalsIgnoreCase(loanof.getRole().trim())) {
 
            throw new RuntimeException("Access Denied: User ID " + loanof + " does not hold managerial authorization authority level.");

        }

        List<LoanApplication> allApplications = loanService.getAllApplications();
        List<LoanApplication> assignedLoans = allApplications.stream()
                .filter(app -> "UNDER_REVIEW".equalsIgnoreCase(app.getStatus()))
                .filter(app -> app.getLoanOfficer() != null && app.getLoanOfficer().getId().equals(officerId))
                .collect(Collectors.toList());

        LoanApplication selectedApp = null;
        if (selectedAppId != null) {
            selectedApp = assignedLoans.stream()
                    .filter(app -> app.getApplicationId().equals(selectedAppId))
                    .findFirst()
                    .orElse(null);
        } else if (!assignedLoans.isEmpty()) {
            selectedApp = assignedLoans.get(0);
        }

        if (selectedApp != null) {
            model.addAttribute("selectedApp", selectedApp);

            List<Document> loanDocuments = documentService.getDocumentsByLoan(selectedApp.getApplicationId());

            if (loanDocuments != null && !loanDocuments.isEmpty()) {
                model.addAttribute("activeDoc", loanDocuments.get(0));
            } else {
                model.addAttribute("activeDoc", null);
            }
            
            System.out.println(loanDocuments.get(0));
            double loanAmt = selectedApp.getLoanAmount().doubleValue();
            double defaultAssetValuation = loanAmt * 1.5; 
            int coverageMarginPercent = (int) ((defaultAssetValuation / loanAmt) * 100);
            
            model.addAttribute("collateralValue", defaultAssetValuation);
            model.addAttribute("coverageMargin", coverageMarginPercent);
            model.addAttribute("marginPassed", coverageMarginPercent >= 120);
        }

        model.addAttribute("assignedLoans", assignedLoans);
        model.addAttribute("activeQueueCount", assignedLoans.size());
        model.addAttribute("officerName", loanof.getName());
        model.addAttribute("officerId", officerId); 
        
        return "officer-dashboard";
    }

    @PostMapping("/evaluate")
    public String processApplicationEvaluation(@RequestParam("applicationId") Integer applicationId,
                                               @RequestParam("creditScore") Integer creditScore,
                                               @RequestParam("riskNotes") String riskNotes,
                                               @RequestParam("actionOutcome") String actionOutcome) {

        LoanApplication application = loanService.getApplicationById(applicationId);
        if (application == null) {
            throw new RuntimeException("Underwriting Error: Selected file reference is missing for ID: " + applicationId);
        }

        LoanOfficer currentOfficer = application.getLoanOfficer();
        Integer currentOfficerId = (currentOfficer != null) ? currentOfficer.getId() : null;

        if (creditScore < 600 || "REJECTED".equalsIgnoreCase(actionOutcome)) {
            application.setStatus("REJECTED");

            if (currentOfficer != null && currentOfficer.getActiveApplicationCount() > 0) {
                currentOfficer.setActiveApplicationCount(currentOfficer.getActiveApplicationCount() - 1);
                officerRepository.save(currentOfficer);
            }
            
        } else if ("ESCALATED".equalsIgnoreCase(actionOutcome)) {
            
            creditAssessmentService.evaluateCredit(application, creditScore, riskNotes);
            System.out.println(" [Credit Engine] Successfully generated credit assessment record for App ID: " + applicationId);

            if (currentOfficer != null && currentOfficer.getActiveApplicationCount() > 0) {
                currentOfficer.setActiveApplicationCount(currentOfficer.getActiveApplicationCount() - 1);
                officerRepository.save(currentOfficer); 
            }

            // B. FETCH LEAST LOADED EXECUTIVE MANAGER OUT OF SYSTEM POOLS
            LoanOfficer assignedManager = officerRepository.findLeastLoadedManager()
                    .orElseThrow(() -> new RuntimeException("Routing Allocation Error: No executive manager available in banking grids."));

            // C. INCREMENT ASSIGNED MANAGER'S WORKLOAD TRACKER
            assignedManager.setActiveApplicationCount(assignedManager.getActiveApplicationCount() + 1);
            officerRepository.save(assignedManager);

            // D. RE-ASSIGN APPLICATION OWNER HANDOFF AND SET CORRESPONDING QUEUE STATUS
            application.setLoanOfficer(assignedManager); // Changes assignment link from Officer -> Manager
            application.setStatus("PENDING");           // Updates status to PENDING for the manager's view
        }

        // 3. Save the modified loan tracking record details down to database tables
        loanService.submitApplication(application, application.getCustomer().getId()); 

        System.out.println("🔵 [Pipeline Handoff Engine] App #" + applicationId + " successfully moved to Status: " + application.getStatus());

        // 4. Reload the workdesk panel safely using the active officer's context path mapping
        if (currentOfficerId != null) {
            return "redirect:/customer/dashboard/officer/workdesk/" + currentOfficerId;
        }
        
        return "redirect:/customer/dashboard/officer/workdesk/1";
    }
    
    @GetMapping("/view/{id}")
    @ResponseBody // Safely intercepts streaming byte arrays directly inside standard view controllers
    public ResponseEntity<byte[]> viewDocumentInline(@PathVariable("id") Integer id) {
        
        // 1. Fetch document out of your dat	a access layer securely
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
        
        byte[] fileBytes = doc.getFileData();
        if (fileBytes == null || fileBytes.length == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // 2. Configure HTTP Headers safely handling special characters and formatting
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(doc.getFileType())); 
        
        // Generates completely safe inline disposition parameters mapping automatically
        ContentDisposition contentDisposition = ContentDisposition.inline()
                .filename(doc.getFileName(), StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(contentDisposition);
        
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        // 3. Send package payload down the line
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
}