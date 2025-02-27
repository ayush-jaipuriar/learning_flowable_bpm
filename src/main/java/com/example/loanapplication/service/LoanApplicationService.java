package com.example.loanapplication.service;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final CmmnRuntimeService cmmnRuntimeService;

    @Transactional
    public LoanApplication submitLoanApplication(LoanApplication loanApplication) {
        // Set initial status
        loanApplication.setStatus("SUBMITTED");
        
        // Save the loan application to get an ID
        LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        
        // Prepare variables for the process
        Map<String, Object> variables = new HashMap<>();
        variables.put("loanApplicationId", savedApplication.getId());
        variables.put("applicantName", savedApplication.getApplicantName());
        variables.put("loanAmount", savedApplication.getLoanAmount());
        variables.put("loanTermMonths", savedApplication.getLoanTermMonths());
        variables.put("monthlyIncome", savedApplication.getMonthlyIncome());
        variables.put("existingDebt", savedApplication.getExistingDebt());
        
        // Start the loan application process
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("loanApplicationProcess", variables);
        
        // Update the loan application with the process instance ID
        savedApplication.setProcessInstanceId(processInstance.getId());
        savedApplication = loanApplicationRepository.save(savedApplication);
        
        // Force a flush to ensure the entity is saved before the process continues
        loanApplicationRepository.flush();
        
        log.info("Started loan application process with ID: {} for applicant: {}", 
                processInstance.getId(), savedApplication.getApplicantName());
        
        return savedApplication;
    }
    
    @Transactional
    public LoanApplication getLoanApplication(Long id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + id));
    }
    
    @Transactional
    public List<LoanApplication> getAllLoanApplications() {
        return loanApplicationRepository.findAll();
    }
    
    @Transactional
    public void startExceptionCase(Long loanApplicationId, String reviewType) {
        LoanApplication loanApplication = getLoanApplication(loanApplicationId);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("loanApplicationId", loanApplicationId);
        variables.put("reviewType", reviewType);
        variables.put("applicantName", loanApplication.getApplicantName());
        variables.put("loanAmount", loanApplication.getLoanAmount());
        variables.put("creditScore", loanApplication.getCreditScore());
        variables.put("monthlyIncome", loanApplication.getMonthlyIncome());
        variables.put("existingDebt", loanApplication.getExistingDebt());
        
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("loanExceptionCase")
                .variables(variables)
                .start();
        
        log.info("Started exception case with ID: {} for loan application: {}", 
                caseInstance.getId(), loanApplicationId);
    }
    
    @Transactional
    public void handleBorderlineApplication(Long loanApplicationId) {
        startExceptionCase(loanApplicationId, "borderline");
    }
    
    @Transactional
    public void handleCustomerInquiry(Long loanApplicationId) {
        startExceptionCase(loanApplicationId, "inquiry");
    }
    
    @Transactional
    public void handleLoanModification(Long loanApplicationId) {
        startExceptionCase(loanApplicationId, "modification");
    }
}
