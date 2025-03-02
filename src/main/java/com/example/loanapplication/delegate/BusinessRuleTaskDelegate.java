package com.example.loanapplication.delegate;

import org.flowable.dmn.api.DmnDecisionQuery;
import org.flowable.dmn.api.DmnDecision;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnDecisionService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Delegate to handle DMN execution directly.
 * This is an alternative to using the businessRuleTask in BPMN.
 */
@Component("businessRuleTaskDelegate")
public class BusinessRuleTaskDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleTaskDelegate.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void execute(DelegateExecution execution) {
        String activityId = execution.getCurrentActivityId();
        logger.info("Executing business rule task: {} (execution ID: {})", activityId, execution.getId());

        try {
            // Determine which DMN decision to execute
            String decisionKey = null;
            if ("evaluateLoanEligibility".equals(activityId)) {
                decisionKey = "loanEligibility";
            } else if ("calculateInterestRate".equals(activityId)) {
                decisionKey = "loanInterestRate";
            } else {
                logger.warn("No specific DMN decision key for activity: {}", activityId);
                return;
            }

            // Get DMN services
            DmnRepositoryService dmnRepositoryService = applicationContext.getBean(DmnRepositoryService.class);
            DmnDecisionService dmnDecisionService = applicationContext.getBean(DmnDecisionService.class);
            
            // Log all available DMN decisions for debugging
            DmnDecisionQuery query = dmnRepositoryService.createDecisionQuery();
            logger.info("Available DMN decisions: {}", query.count());
            for (DmnDecision decision : query.list()) {
                logger.info("Found DMN decision: {} (key: {}, id: {}, version: {})", 
                        decision.getName(), decision.getKey(), decision.getId(), decision.getVersion());
            }
            
            // Find the specific decision we need
            DmnDecision decision = dmnRepositoryService.createDecisionQuery()
                    .decisionKey(decisionKey)
                    .latestVersion()
                    .singleResult();
            
            if (decision == null) {
                logger.error("DMN decision not found: {}", decisionKey);
                applyFallbackLogic(execution, decisionKey);
                return;
            }
            
            logger.info("Found DMN decision: {} (id: {})", decision.getName(), decision.getId());
            
            // Prepare input variables
            Map<String, Object> variables = new HashMap<>(execution.getVariables());
            
            // Execute DMN decision using the correct method
            logger.info("Executing DMN decision: {} with variables: {}", decisionKey, variables);
            List<Map<String, Object>> resultList = dmnDecisionService.createExecuteDecisionBuilder()
                    .decisionKey(decisionKey)
                    .variables(variables)
                    .execute();
            
            // Process result
            if (resultList != null && !resultList.isEmpty()) {
                logger.info("DMN decision result: {}", resultList);
                // Add result variables to execution - use the first result
                Map<String, Object> result = resultList.get(0);
                execution.setVariables(result);
            } else {
                logger.warn("No result from DMN decision: {}", decisionKey);
                applyFallbackLogic(execution, decisionKey);
            }
            
            logger.info("Successfully executed DMN decision: {}", decisionKey);
        } catch (Exception e) {
            logger.error("Error executing DMN decision for activity: {}", activityId, e);
            // Apply fallback logic based on activity ID
            if ("evaluateLoanEligibility".equals(activityId)) {
                execution.setVariable("eligible", false);
                execution.setVariable("reason", "Error in eligibility evaluation - defaulting to ineligible");
            } else if ("calculateInterestRate".equals(activityId)) {
                execution.setVariable("interestRate", 15.0);
                execution.setVariable("riskLevel", "High");
            }
        }
    }
    
    private void applyFallbackLogic(DelegateExecution execution, String decisionKey) {
        if ("loanEligibility".equals(decisionKey)) {
            execution.setVariable("eligible", false);
            execution.setVariable("reason", "No result from eligibility decision - defaulting to ineligible");
        } else if ("loanInterestRate".equals(decisionKey)) {
            execution.setVariable("interestRate", 15.0);
            execution.setVariable("riskLevel", "High");
        }
    }
} 