package com.example.loanapplication.config;

import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;

@Configuration
public class FlowableConfiguration {
    
    @Bean
    public ProcessEngineConfiguration processEngineConfiguration(SpringProcessEngineConfiguration configuration, 
                                                                ResourceLoader resourceLoader) throws IOException {
        // Configure Flowable to use Spring beans for delegation
        configuration.setEnableProcessDefinitionInfoCache(true);
        
        // Load BPMN process definitions
        Resource[] processResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources("classpath*:processes/*.bpmn20.xml");
        
        if (processResources != null && processResources.length > 0) {
            configuration.setDeploymentResources(processResources);
        }
        
        // Load DMN decision tables
        Resource[] dmnResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources("classpath*:dmn/*.dmn");
        
        if (dmnResources != null && dmnResources.length > 0) {
            // Add DMN resources to deployment resources
            Resource[] allResources = new Resource[processResources.length + dmnResources.length];
            System.arraycopy(processResources, 0, allResources, 0, processResources.length);
            System.arraycopy(dmnResources, 0, allResources, processResources.length, dmnResources.length);
            configuration.setDeploymentResources(allResources);
        }
        
        return configuration;
    }
}
