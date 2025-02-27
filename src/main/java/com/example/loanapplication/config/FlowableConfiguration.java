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
        Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources("classpath*:processes/*.bpmn20.xml");
        
        if (resources != null && resources.length > 0) {
            configuration.setDeploymentResources(resources);
        }
        
        return configuration;
    }
}
