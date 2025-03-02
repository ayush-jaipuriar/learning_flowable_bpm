package com.example.loanapplication.service;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Service to handle DMN fallback logic.
 * Implements JavaDelegate so it can be used directly in BPMN process.
 */
@Service
public class DmnFallbackService implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DmnFallbackService.class);

    // Use ApplicationContext instead of direct RuntimeService injection to avoid circular dependency
    @Autowired
    private ApplicationContext applicationContext;
    
    // Lazy getter for RuntimeService
    private RuntimeService getRuntimeService() {
        return applicationContext.getBean(RuntimeService.class);
    }
    
    @Override
    public void execute(DelegateExecution execution) {
        String activityId = execution.getCurrentActivityId();
        logger.info("Executing fallback for DMN task: {} (execution ID: {})", activityId, execution.getId());
        
        try {
            // Determine which DMN task failed and apply appropriate fallback logic
            if ("evaluateLoanEligibility".equals(activityId)) {
                logger.info("Applying fallback for loan eligibility evaluation");
                // Default to ineligible for safety
                execution.setVariable("eligible", false);
                execution.setVariable("reason", "Error in eligibility evaluation - defaulting to ineligible");
            } else if ("calculateInterestRate".equals(activityId)) {
                logger.info("Applying fallback for interest rate calculation");
                // Default to high interest rate for safety
                execution.setVariable("interestRate", 15.0);
                execution.setVariable("riskLevel", "High");
            } else {
                logger.warn("No specific fallback implementation for task: {}", activityId);
            }
            
            logger.info("Successfully applied fallback logic for DMN task: {}", activityId);
        } catch (Exception e) {
            logger.error("Error executing fallback for DMN task: {}", activityId, e);
            // Set error variables but don't throw exception to allow process to continue
            execution.setVariable("fallbackError", true);
            execution.setVariable("fallbackErrorMessage", e.getMessage());
        }
    }
    
    /**
     * Legacy method for backward compatibility
     */
    public void executeFallback(String executionId, String taskName) {
        logger.info("Legacy executeFallback called for task: {} (execution ID: {})", taskName, executionId);
        // This method is kept for backward compatibility but implementation is simplified
        // since the JavaDelegate.execute method is now the primary implementation
    }
}
