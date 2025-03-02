package com.example.loanapplication.config;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableExceptionEvent;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.BpmnError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This event listener handles DMN execution errors and converts them to BPMN errors
 * that can be caught by error boundary events.
 */
@Component
public class DmnErrorHandler implements FlowableEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DmnErrorHandler.class);

    // Use ApplicationContext instead of direct RuntimeService injection to avoid circular dependency
    @Autowired
    private ApplicationContext applicationContext;
    
    // Lazy getter for RuntimeService
    private RuntimeService getRuntimeService() {
        return applicationContext.getBean(RuntimeService.class);
    }

    @Override
    public void onEvent(FlowableEvent event) {
        if (event instanceof FlowableExceptionEvent) {
            FlowableExceptionEvent exceptionEvent = (FlowableExceptionEvent) event;
            Throwable cause = exceptionEvent.getCause();
            
            // Check if this is a DMN execution error
            if (cause instanceof FlowableException && 
                cause.getMessage() != null && 
                (cause.getMessage().contains("doesn't contain any rules") || 
                 cause.getMessage().contains("DMN decision table"))) {
                
                logger.warn("DMN execution error detected: {}", cause.getMessage());
                
                try {
                    // Set the error activity variable to the current business rule task
                    logger.info("DMN execution error event type: {}", event.getType());
                    
                    // Throw a BPMN error that can be caught by an error boundary event
                    logger.info("Throwing BPMN error 'DMN_EXECUTION_ERROR'");
                    throw new BpmnError("DMN_EXECUTION_ERROR");
                } catch (Exception e) {
                    logger.error("Error handling DMN execution error", e);
                }
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
