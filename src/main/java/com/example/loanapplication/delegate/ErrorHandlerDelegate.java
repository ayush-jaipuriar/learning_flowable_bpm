package com.example.loanapplication.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This delegate handles errors that occur during process execution.
 */
@Component("errorHandlerDelegate")
public class ErrorHandlerDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlerDelegate.class);

    @Autowired
    private EvaluateLoanEligibilityDelegate evaluateLoanEligibilityDelegate;

    @Autowired
    private CalculateInterestRateDelegate calculateInterestRateDelegate;

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("ErrorHandlerDelegate called");

        try {
            // Get the activity where the error occurred
            String errorActivity = (String) execution.getVariable("currentBusinessRuleTask");
            
            if (errorActivity == null) {
                logger.warn("No error activity specified, cannot handle error");
                return;
            }
            
            logger.info("Handling error for activity: {}", errorActivity);
            
            // Determine which business rule task failed and execute the corresponding delegate
            if ("evaluateLoanEligibility".equals(errorActivity)) {
                logger.info("Falling back to Java implementation for loan eligibility evaluation");
                evaluateLoanEligibilityDelegate.execute(execution);
            } else if ("calculateInterestRate".equals(errorActivity)) {
                logger.info("Falling back to Java implementation for interest rate calculation");
                calculateInterestRateDelegate.execute(execution);
            } else {
                logger.warn("No fallback implementation available for activity: {}", errorActivity);
            }
        } catch (Exception e) {
            logger.error("Error in ErrorHandlerDelegate", e);
            throw e;
        }
    }
}
