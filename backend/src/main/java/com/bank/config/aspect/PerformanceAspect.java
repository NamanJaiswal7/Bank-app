package com.bank.config.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Aspect for monitoring performance of application handlers and external calls.
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    @Pointcut("within(com.bank.application.handler..*) || within(com.bank.adapter.out.external..*)")
    public void performancePointcut() {}

    @Around("performancePointcut()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = joinPoint.proceed();

        stopWatch.stop();
        log.info("Execution time of {}.{}() :: {} ms",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                stopWatch.getTotalTimeMillis());
        return result;
    }
}
