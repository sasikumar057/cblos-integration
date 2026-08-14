package com.cblos.webController;

import com.cblos.model.CorporateCustomer;
import com.cblos.model.LoanOfficer;
import com.cblos.service.AdminService;
import com.cblos.service.LoanOfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardViewController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private LoanOfficerService officerService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {

        List<CorporateCustomer> pendingCustomers = adminService.getPendingRegistrations();
        model.addAttribute("pendingCustomers", pendingCustomers);

        List<LoanOfficer> internalStaff = officerService.getAllLoanOfficer();
        model.addAttribute("internalStaff", internalStaff);

        model.addAttribute("newStaff", new LoanOfficer());

        return "admin-dashboard";
    }

    @PostMapping("/dashboard/customer/review/{customerId}")
    public String processCustomerReview(@PathVariable("customerId") Integer customerId,
                                        @RequestParam("approve") boolean approve,
                                        @RequestParam(value = "reason", required = false) String reason) {
        
        adminService.reviewCustomerRegistration(customerId, approve, reason);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/staff/onboard")
    public String processStaffOnboarding(@ModelAttribute("newStaff") LoanOfficer staffDetails,
                                         @RequestParam("employeeEmail") String employeeEmail) {
        
        adminService.onboardBankStaff(staffDetails, employeeEmail);
        return "redirect:/admin/dashboard";
    }
}
