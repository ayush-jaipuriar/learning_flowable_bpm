package com.example.loanapplication.config;

import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.engine.DmnEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;

@Configuration
public class DmnDeploymentConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DmnDeploymentConfiguration.class);

    @Autowired(required = false)
    private DmnEngine dmnEngine;

    @PostConstruct
    public void deployDmnResources() {
        if (dmnEngine == null) {
            logger.warn("DMN engine not available, skipping DMN deployment");
            return;
        }
        
        try {
            DmnRepositoryService dmnRepositoryService = dmnEngine.getDmnRepositoryService();
            
            // Check if DMN resources exist
            boolean loanEligibilityExists = new ClassPathResource("dmn/loan-eligibility.dmn").exists();
            boolean interestRateExists = new ClassPathResource("dmn/loan-interest-rate.dmn").exists();
            
            if (loanEligibilityExists && interestRateExists) {
                logger.info("Found DMN resources to deploy");
                
                // Create a deployment with DMN resources
                dmnRepositoryService.createDeployment()
                        .name("LoanApplicationDMN")
                        .addClasspathResource("dmn/loan-eligibility.dmn")
                        .addClasspathResource("dmn/loan-interest-rate.dmn")
                        .deploy();
                
                logger.info("Successfully deployed DMN resources");
            } else {
                logger.warn("One or more DMN resources not found: loan-eligibility.dmn ({}), loan-interest-rate.dmn ({})",
                        loanEligibilityExists, interestRateExists);
            }
        } catch (Exception e) {
            logger.error("Error deploying DMN resources", e);
        }
    }
}
