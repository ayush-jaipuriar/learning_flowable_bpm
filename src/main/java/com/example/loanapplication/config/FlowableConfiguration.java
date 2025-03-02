package com.example.loanapplication.config;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.dmn.spring.SpringDmnEngineConfiguration;
import org.flowable.dmn.spring.configurator.SpringDmnEngineConfigurator;
import org.flowable.cmmn.spring.SpringCmmnEngineConfiguration;
import org.flowable.cmmn.spring.configurator.SpringCmmnEngineConfigurator;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class FlowableConfiguration implements ApplicationListener<ContextRefreshedEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowableConfiguration.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private DmnErrorHandler dmnErrorHandler;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    private boolean eventListenersRegistered = false;
    
    /**
     * Register event listeners after the entire application context is refreshed
     * to avoid circular dependencies during startup.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!eventListenersRegistered) {
            try {
                logger.info("Registering Flowable event listeners after context refresh");
                ProcessEngine processEngine = applicationContext.getBean(ProcessEngine.class);
                
                // Register the DMN error handler for exception events
                processEngine.getProcessEngineConfiguration().getEventDispatcher().addEventListener(
                    dmnErrorHandler, 
                    FlowableEngineEventType.JOB_EXECUTION_FAILURE,
                    FlowableEngineEventType.ACTIVITY_ERROR_RECEIVED,
                    FlowableEngineEventType.PROCESS_COMPLETED_WITH_ERROR_END_EVENT
                );
                
                // Explicitly deploy DMN resources
                try {
                    deployDmnResources();
                    deployCmmnResources(); // Add CMMN deployment
                } catch (Exception e) {
                    logger.error("Failed to deploy resources", e);
                }
                
                eventListenersRegistered = true;
                logger.info("Successfully registered Flowable event listeners");
            } catch (Exception e) {
                logger.error("Failed to register Flowable event listeners", e);
            }
        }
    }
    
    /**
     * Explicitly deploy DMN resources to ensure they are available
     */
    private void deployDmnResources() throws IOException {
        logger.info("Explicitly deploying DMN resources");
        
        // Get DMN repository service
        DmnRepositoryService dmnRepositoryService = applicationContext.getBean(DmnRepositoryService.class);
        
        // Load DMN resources
        Resource[] dmnResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources("classpath*:dmn/*.dmn");
        
        if (dmnResources.length > 0) {
            logger.info("Found {} DMN resources to deploy", dmnResources.length);
            
            // Deploy each DMN resource
            for (Resource resource : dmnResources) {
                try {
                    String resourceName = resource.getFilename();
                    logger.info("Deploying DMN resource: {}", resourceName);
                    
                    dmnRepositoryService.createDeployment()
                            .name("LoanApplicationDMN")
                            .addInputStream(resourceName, resource.getInputStream())
                            .deploy();
                    
                    logger.info("Successfully deployed DMN resource: {}", resourceName);
                } catch (Exception e) {
                    logger.error("Failed to deploy DMN resource: {}", resource.getFilename(), e);
                }
            }
        } else {
            logger.warn("No DMN resources found to deploy");
        }
    }
    
    /**
     * Explicitly deploy CMMN resources to ensure they are available
     */
    private void deployCmmnResources() throws IOException {
        logger.info("Explicitly deploying CMMN resources");
        
        try {
            // Get CMMN repository service
            CmmnRepositoryService cmmnRepositoryService = applicationContext.getBean(CmmnRepositoryService.class);
            
            // Load CMMN resources
            Resource[] cmmnResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                    .getResources("classpath*:cases/*.cmmn");
            
            if (cmmnResources.length > 0) {
                logger.info("Found {} CMMN resources to deploy", cmmnResources.length);
                
                // Deploy each CMMN resource
                for (Resource resource : cmmnResources) {
                    try {
                        String resourceName = resource.getFilename();
                        logger.info("Deploying CMMN resource: {}", resourceName);
                        
                        cmmnRepositoryService.createDeployment()
                                .name("LoanApplicationCMMN")
                                .addInputStream(resourceName, resource.getInputStream())
                                .deploy();
                        
                        logger.info("Successfully deployed CMMN resource: {}", resourceName);
                    } catch (Exception e) {
                        logger.error("Failed to deploy CMMN resource: {}", resource.getFilename(), e);
                    }
                }
            } else {
                logger.warn("No CMMN resources found to deploy");
            }
        } catch (Exception e) {
            logger.error("Failed to deploy CMMN resources", e);
        }
    }
    
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> customProcessEngineConfigurer() {
        return engineConfiguration -> {
            // Set up a combined deployment for BPMN and DMN resources
            engineConfiguration.setDeploymentMode("resource-parent-folder");
            
            // Enable rule execution tracing for debugging
            logger.info("Setting up DMN engine...");
            
            // Configure DMN engine
            SpringDmnEngineConfigurator dmnEngineConfigurator = new SpringDmnEngineConfigurator();
            SpringDmnEngineConfiguration dmnEngineConfiguration = new SpringDmnEngineConfiguration();
            dmnEngineConfiguration.setDeploymentName("LoanApplicationDMN");
            
            dmnEngineConfigurator.setDmnEngineConfiguration(dmnEngineConfiguration);
            engineConfiguration.addConfigurator(dmnEngineConfigurator);
            
            // Configure CMMN engine
            logger.info("Setting up CMMN engine...");
            SpringCmmnEngineConfigurator cmmnEngineConfigurator = new SpringCmmnEngineConfigurator();
            SpringCmmnEngineConfiguration cmmnEngineConfiguration = new SpringCmmnEngineConfiguration();
            cmmnEngineConfiguration.setDeploymentName("LoanApplicationCMMN");
            
            cmmnEngineConfigurator.setCmmnEngineConfiguration(cmmnEngineConfiguration);
            engineConfiguration.addConfigurator(cmmnEngineConfigurator);
        };
    }
    
    @Bean
    public ProcessEngineConfiguration processEngineConfiguration(SpringProcessEngineConfiguration configuration, 
                                                                 ResourceLoader resourceLoader) throws IOException {
        // Configure Flowable to use Spring beans for delegation
        configuration.setEnableProcessDefinitionInfoCache(true);
        
        // Load BPMN process definitions, DMN decision tables, and CMMN case models
        Resource[] processResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources("classpath*:processes/*.bpmn20.xml");
        
        Resource[] dmnResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources("classpath*:dmn/*.dmn");
                
        Resource[] cmmnResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources("classpath*:cases/*.cmmn");
        
        logger.info("Found {} BPMN resources, {} DMN resources, and {} CMMN resources", 
                processResources.length, dmnResources.length, cmmnResources.length);
        
        // Create a combined list for deployment
        List<Resource> allResources = new ArrayList<>();
        for (Resource processResource : processResources) {
            allResources.add(processResource);
            logger.info("Added BPMN resource: {}", processResource.getFilename());
        }
        
        for (Resource dmnResource : dmnResources) {
            allResources.add(dmnResource);
            logger.info("Added DMN resource: {}", dmnResource.getFilename());
        }
        
        for (Resource cmmnResource : cmmnResources) {
            allResources.add(cmmnResource);
            logger.info("Added CMMN resource: {}", cmmnResource.getFilename());
        }
        
        if (!allResources.isEmpty()) {
            Resource[] deploymentResources = allResources.toArray(new Resource[0]);
            logger.info("Setting up deployment with {} resources", deploymentResources.length);
            configuration.setDeploymentResources(deploymentResources);
        }
        
        return configuration;
    }
}
