package ru.tn.error.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Aspect
@Component
public class ErrorHandlerAspect {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    private Tracer tracer;

    @AfterThrowing(pointcut = "execution(public tu.tn.*.*(..))", argNames = "ex")
    public void handleError(Throwable ex) {
        log.error("Error has occurred", ex);
        executorService.submit(() -> {
            try {
                Span span = tracer.getCurrentSpan();
                if(span != null) {
                    span.getTraceId();
                }
            } catch (Throwable th) {
                log.error("Error while send mail", th);
                throw new RuntimeException(th);
            }
        });
    }
}
