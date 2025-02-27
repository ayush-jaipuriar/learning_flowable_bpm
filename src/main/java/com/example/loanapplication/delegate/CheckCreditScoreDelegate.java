package com.example.loanapplication.delegate;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.repository.LoanApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class CheckCreditScoreDelegate implements JavaDelegate {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    
    private final Random random = new Random();
    
    // Default no-args constructor for Flowable
    public CheckCreditScoreDelegate() {
    }

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        log.info("Checking credit score for loan application with process instance ID: {}", processInstanceId);
        
        // Retrieve loan application from database
        LoanApplication loanApplication = loanApplicationRepository.findByProcessInstanceId(processInstanceId);
        
        // If not found by process instance ID, try to find by loan application ID from variables
        if (loanApplication == null && execution.hasVariable("loanApplicationId")) {
            Long loanApplicationId = (Long) execution.getVariable("loanApplicationId");
            log.info("Loan application not found by process instance ID, trying with loan application ID: {}", loanApplicationId);
            loanApplication = loanApplicationRepository.findById(loanApplicationId)
                    .orElse(null);
            
            // If found, update the process instance ID
            if (loanApplication != null) {
                loanApplication.setProcessInstanceId(processInstanceId);
                loanApplicationRepository.save(loanApplication);
                log.info("Updated loan application with process instance ID");
            }
        }
        
        if (loanApplication == null) {
            throw new RuntimeException("Loan application not found for process instance ID: " + processInstanceId);
        }
        
        // In a real-world scenario, this would call a credit bureau API
        // For this example, we'll simulate a credit score check
        int creditScore = simulateCreditScoreCheck(loanApplication);
        loanApplication.setCreditScore(creditScore);
        
        // Save the updated loan application
        loanApplicationRepository.save(loanApplication);
        
        // Set variables for the process
        execution.setVariable("creditScore", creditScore);
        execution.setVariable("monthlyIncome", loanApplication.getMonthlyIncome());
        execution.setVariable("existingDebt", loanApplication.getExistingDebt());
        execution.setVariable("loanAmount", loanApplication.getLoanAmount());
        
        log.info("Credit score for applicant {}: {}", loanApplication.getApplicantName(), creditScore);
    }
    
    private int simulateCreditScoreCheck(LoanApplication loanApplication) {
        // This is a simplified simulation of a credit score check
        // In a real application, this would call a credit bureau API
        
        // Base score between 500 and 800
        int baseScore = 500 + random.nextInt(301);
        
        // Adjust based on income (higher income = slightly better score)
        double incomeAdjustment = Math.min(50, loanApplication.getMonthlyIncome().doubleValue() / 100);
        
        // Adjust based on existing debt (higher debt = slightly worse score)
        double debtAdjustment = Math.min(50, loanApplication.getExistingDebt().doubleValue() / 50);
        
        // Calculate final score
        int finalScore = (int) (baseScore + incomeAdjustment - debtAdjustment);
        
        // Ensure score is within valid range (300-850)
        return Math.max(300, Math.min(850, finalScore));
    }
}
