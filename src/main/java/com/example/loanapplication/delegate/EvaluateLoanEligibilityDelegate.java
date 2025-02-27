package com.example.loanapplication.delegate;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.repository.LoanApplicationRepository;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EvaluateLoanEligibilityDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(EvaluateLoanEligibilityDelegate.class);

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("Evaluating loan eligibility for process instance ID: {}", execution.getProcessInstanceId());

        // Find the loan application by process instance ID
        LoanApplication loanApplication = loanApplicationRepository.findByProcessInstanceId(execution.getProcessInstanceId());
        
        if (loanApplication == null) {
            logger.error("Loan application not found for process instance ID: {}", execution.getProcessInstanceId());
            return;
        }

        // Get credit score, monthly income, and existing debt from the loan application
        int creditScore = loanApplication.getCreditScore();
        double monthlyIncome = loanApplication.getMonthlyIncome().doubleValue();
        double existingDebt = loanApplication.getExistingDebt().doubleValue();
        double loanAmount = loanApplication.getLoanAmount().doubleValue();

        logger.info("Evaluating eligibility for applicant {} with credit score: {}, monthly income: {}, existing debt: {}, loan amount: {}",
                loanApplication.getApplicantName(), creditScore, monthlyIncome, existingDebt, loanAmount);

        // Implement the same logic as in the DMN decision table
        boolean eligible = false;
        String reason = "";

        // Rule 1: Credit score < 550 -> Not eligible
        if (creditScore < 550) {
            eligible = false;
            reason = "Credit score too low";
        }
        // Rule 2: Credit score >= 550 but monthly income < 2000 -> Not eligible
        else if (monthlyIncome < 2000) {
            eligible = false;
            reason = "Monthly income too low";
        }
        // Rule 3: Credit score >= 550, monthly income >= 2000, but debt-to-income ratio > 0.5 -> Not eligible
        else if ((existingDebt / monthlyIncome) > 0.5) {
            eligible = false;
            reason = "Debt-to-income ratio too high";
        }
        // Rule 4: All other cases -> Eligible
        else {
            eligible = true;
            reason = "Meets all eligibility criteria";
        }

        logger.info("Eligibility result for {}: eligible={}, reason={}", 
                loanApplication.getApplicantName(), eligible, reason);

        // Set process variables
        execution.setVariable("eligible", eligible);
        execution.setVariable("eligibilityReason", reason);

        // Update loan application status
        loanApplication.setStatus(eligible ? "ELIGIBLE" : "REJECTED");
        loanApplicationRepository.save(loanApplication);
    }
}
