package com.example.loanapplication.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This delegate acts as a fallback mechanism for business rule tasks.
 * It will be called if the DMN rule execution fails.
 */
@Component("businessRuleTaskFallbackDelegate")
public class BusinessRuleTaskFallbackDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleTaskFallbackDelegate.class);

    @Autowired
    private EvaluateLoanEligibilityDelegate evaluateLoanEligibilityDelegate;

    @Autowired
    private CalculateInterestRateDelegate calculateInterestRateDelegate;

    @Override
    public void execute(DelegateExecution execution) {
        String currentActivityId = execution.getCurrentActivityId();
        logger.info("BusinessRuleTaskFallbackDelegate called for activity: {}", currentActivityId);

        try {
            // Determine which business rule task failed and execute the corresponding delegate
            if ("evaluateLoanEligibility".equals(currentActivityId)) {
                logger.info("Falling back to Java implementation for loan eligibility evaluation");
                evaluateLoanEligibilityDelegate.execute(execution);
            } else if ("calculateInterestRate".equals(currentActivityId)) {
                logger.info("Falling back to Java implementation for interest rate calculation");
                calculateInterestRateDelegate.execute(execution);
            } else {
                logger.warn("No fallback implementation available for activity: {}", currentActivityId);
            }
        } catch (Exception e) {
            logger.error("Error in BusinessRuleTaskFallbackDelegate for activity: {}", currentActivityId, e);
            throw e;
        }
    }
}
