package com.example.loanapplication.delegate;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.repository.LoanApplicationRepository;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CalculateInterestRateDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CalculateInterestRateDelegate.class);

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("Calculating interest rate for process instance ID: {}", execution.getProcessInstanceId());

        // Find the loan application by process instance ID
        LoanApplication loanApplication = loanApplicationRepository.findByProcessInstanceId(execution.getProcessInstanceId());
        
        if (loanApplication == null) {
            logger.error("Loan application not found for process instance ID: {}", execution.getProcessInstanceId());
            return;
        }

        // Get credit score, loan amount, and loan term from the loan application
        int creditScore = loanApplication.getCreditScore();
        BigDecimal loanAmountBigDecimal = loanApplication.getLoanAmount();
        double loanAmount = loanAmountBigDecimal.doubleValue(); // Convert BigDecimal to double
        int loanTermMonths = loanApplication.getLoanTermMonths();

        logger.info("Calculating interest rate for applicant {} with credit score: {}, loan amount: {}, loan term: {} months",
                loanApplication.getApplicantName(), creditScore, loanAmount, loanTermMonths);

        // Implement the same logic as in the DMN decision table for interest rate
        double interestRate;
        String riskLevel;

        // Credit Score 550-649 (High Risk)
        if (creditScore >= 550 && creditScore <= 649) {
            if (loanAmount <= 10000) {
                if (loanTermMonths <= 24) {
                    interestRate = 12.5;
                    riskLevel = "High";
                } else {
                    interestRate = 13.5;
                    riskLevel = "High";
                }
            } else {
                if (loanTermMonths <= 24) {
                    interestRate = 13.0;
                    riskLevel = "High";
                } else {
                    interestRate = 14.0;
                    riskLevel = "High";
                }
            }
        }
        // Credit Score 650-699 (Medium Risk)
        else if (creditScore >= 650 && creditScore <= 699) {
            if (loanAmount <= 10000) {
                if (loanTermMonths <= 24) {
                    interestRate = 9.5;
                    riskLevel = "Medium";
                } else {
                    interestRate = 10.5;
                    riskLevel = "Medium";
                }
            } else {
                if (loanTermMonths <= 24) {
                    interestRate = 10.0;
                    riskLevel = "Medium";
                } else {
                    interestRate = 11.0;
                    riskLevel = "Medium";
                }
            }
        }
        // Credit Score 700+ (Low Risk)
        else {
            if (loanAmount <= 10000) {
                if (loanTermMonths <= 24) {
                    interestRate = 6.5;
                    riskLevel = "Low";
                } else {
                    interestRate = 7.5;
                    riskLevel = "Low";
                }
            } else {
                if (loanTermMonths <= 24) {
                    interestRate = 7.0;
                    riskLevel = "Low";
                } else {
                    interestRate = 8.0;
                    riskLevel = "Low";
                }
            }
        }

        logger.info("Interest rate for {}: {}%, risk level: {}", 
                loanApplication.getApplicantName(), interestRate, riskLevel);

        // Set process variables
        execution.setVariable("interestRate", interestRate);
        execution.setVariable("riskLevel", riskLevel);

        // Update loan application
        loanApplication.setApprovedInterestRate(BigDecimal.valueOf(interestRate));
        loanApplicationRepository.save(loanApplication);
    }
}
