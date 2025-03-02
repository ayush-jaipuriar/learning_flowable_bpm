package com.example.loanapplication.listener;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This listener is attached to business rule tasks to track the current activity
 * in case an error occurs and fallback is needed.
 */
@Component("businessRuleTaskListener")
public class BusinessRuleTaskListener implements ExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleTaskListener.class);

    @Override
    public void notify(DelegateExecution execution) {
        String currentActivityId = execution.getCurrentActivityId();
        logger.info("BusinessRuleTaskListener: Setting current activity ID: {}", currentActivityId);
        
        // Store the current activity ID in a process variable for error handling
        execution.setVariable("currentBusinessRuleTask", currentActivityId);
    }
}
