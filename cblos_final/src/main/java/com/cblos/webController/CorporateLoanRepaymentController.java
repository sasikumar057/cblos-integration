package com.cblos.webController;

import com.cblos.model.LoanAccount;
import com.cblos.model.RepaymentSchedule;
import com.cblos.repository.LoanAccountRepository;
import com.cblos.repository.RepaymentScheduleRepository;
import com.cblos.service.ApprovalService;
import com.cblos.service.DisbursementService; 
import com.cblos.service.LoanApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer/dashboard/loan")
public class CorporateLoanRepaymentController {

    @Autowired
    private LoanApplicationService loanService;
    
    @Autowired
    private LoanAccountRepository accountRepository;
    
    @Autowired
    private ApprovalService approvalService;
    
    @Autowired
    private DisbursementService disbursementService; 
    
    @Autowired
    private RepaymentScheduleRepository scheduleRepository;


    @GetMapping("/repay-page/{accountId}")
    public String showRepaymentPage(@PathVariable("accountId") Integer accountId, Model model) {
        
        // 1. Fetch the core active loan account details
        LoanAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Accounting Error: Outstanding loan account not found."));
        
        // 2. Fetch the FULL schedule record bundle using your existing repo layout
        List<RepaymentSchedule> fullSchedule = scheduleRepository.findByLoanAccount_Id(accountId);
        
        // 3. FILTER IN MEMORY: Extract only the rows where status is "UNPAID" or "PENDING"
        List<RepaymentSchedule> unpaidInstallments = fullSchedule.stream()
                .filter(schedule -> "PENDING".equalsIgnoreCase(schedule.getStatus()) || "UNPAID".equalsIgnoreCase(schedule.getStatus()))
                .collect(Collectors.toList());
        
        // 4. Determine parameters for the immediate upcoming monthly EMI segment block
        BigDecimal monthlyEmi = BigDecimal.ZERO;
        Integer nextInstallmentId = null;
        RepaymentSchedule nextInstallment = null; // 🟢 CHANGE 1: Initialize an object holder
        
        if (!unpaidInstallments.isEmpty()) {
            RepaymentSchedule nextMonth = unpaidInstallments.get(0);
            monthlyEmi = nextMonth.getInstallmentAmount(); 
            
            // 🟢 CHANGE 2: Fetch the true Database Primary Key ID (.getId()), NOT the loop counter sequence number
            nextInstallmentId = nextMonth.getId(); 
            
            // 🟢 CHANGE 3: Store the whole object reference to pass to Thymeleaf
            nextInstallment = nextMonth; 
        }

        // 5. Inject model context values safely for Thymeleaf rendering
        model.addAttribute("account", account);
        model.addAttribute("unpaidInstallments", unpaidInstallments);
        model.addAttribute("monthlyEmi", monthlyEmi);
        model.addAttribute("nextInstallmentId", nextInstallmentId);
        model.addAttribute("totalOutstanding", account.getPrincipalAmount()); 
        
        // 🟢 CHANGE 4: Add the installment object to the model container
        model.addAttribute("nextInstallment", nextInstallment); 

        return "loan-repayment"; 
    }

    /**
     * Processes the reduction calculations and redirects cleanly back to the corporate dashboard.
     * Route: POST /customer/dashboard/loan/repay
     */
    @PostMapping("/repay")
    public String executeInstallmentPayment(@RequestParam("accountId") Integer accountId,
                                            @RequestParam("customerId") Integer customerId,
                                            @RequestParam(value = "installmentId", required = false) Integer installmentId,
                                            @RequestParam("paymentType") String paymentType, // "MONTHLY" or "TOTAL"
                                            @RequestParam("paymentAmount") String paymentAmount) {
        
        LoanAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Accounting System Error: Active Loan account not found."));

        String sanitizedAmount = paymentAmount.replaceAll(",", "");
        BigDecimal payment = new BigDecimal(sanitizedAmount);

        if ("MONTHLY".equalsIgnoreCase(paymentType)) {
            // 💰 PATH A: SINGLE MONTH REPAYMENT
            if (installmentId == null) {
                throw new RuntimeException("Validation Failure: Missing target installment reference pointer.");
            }
            
            // 🟢 FIX 3: Changed the uppercase class static type reference to our injected instance bean handle!
            disbursementService.updateInstallmentStatusSecurely(accountId, installmentId, "PAID");
            
            // Deduct the single payment out of your main remaining principal balance counter
            BigDecimal balanceAfterEmi = account.getPrincipalAmount().subtract(payment);
            account.setPrincipalAmount(balanceAfterEmi.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : balanceAfterEmi);
            
            System.out.println("✅ [Ledger System] Installment #" + installmentId + " successfully marked PAID via bean processor.");

        } else if ("TRANS_TOTAL".equalsIgnoreCase(paymentType) || "TOTAL".equalsIgnoreCase(paymentType)) {
            // 🏦 PATH B: SETTLE AND CLOSE FULL LOAN ACCOUNT BALANCE ENTIRELY
            account.setPrincipalAmount(BigDecimal.ZERO);
            account.setStatus("SETTLED_CLOSED");

            List<RepaymentSchedule> fullSchedule = scheduleRepository.findByLoanAccount_Id(accountId);
            
            for (RepaymentSchedule schedule : fullSchedule) {
                if ("PENDING".equalsIgnoreCase(schedule.getStatus()) || "UNPAID".equalsIgnoreCase(schedule.getStatus())) {
                    schedule.setStatus("PAID_PRE_CLOSURE");
                    scheduleRepository.save(schedule); 
                }
            }
            System.out.println("🎉 [Ledger System] Full pre-closure committed. All installments finalized for Account: " + accountId);
        }

        // Save current updated loan ledger entity properties back down
        accountRepository.save(account);

        return "redirect:/customer/dashboard/" + customerId;
    }
}