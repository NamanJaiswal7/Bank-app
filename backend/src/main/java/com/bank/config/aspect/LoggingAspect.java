package com.bank.config.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging execution of handlers and web controllers.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.bank.application.handler..*) || within(com.bank.adapter.in.web..*)")
    public void loggingPointcut() {}

    @Before("loggingPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Entering: {}.{}() with argument[s] = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}() with message = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getMessage(), e);
    }
}
