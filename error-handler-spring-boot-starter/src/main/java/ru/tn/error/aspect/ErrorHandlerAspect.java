package ru.tn.error.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@Slf4j
@Aspect
@Component
public class ErrorHandlerAspect {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final String ZIPKIN_SERVICE_ID = "zipkin-server";

    private final Tracer tracer;
    private final MailSender mailSender;
    private final DiscoveryClient discoveryClient;

    @Value("${mail.recipients}")
    private List<String> recipients = new ArrayList<>();

    @Value("classpath:/error_message.template")
    private Resource messageTpl;

    @Autowired
    public ErrorHandlerAspect(Tracer tracer, MailSender mailSender, DiscoveryClient discoveryClient) {
        this.tracer = tracer;
        this.mailSender = mailSender;
        this.discoveryClient = discoveryClient;
    }


    @AfterThrowing(pointcut = "execution(public ru.tn.*.*(..)) && within(org.springframework.data.rest.core.annotation.RepositoryRestResource)", argNames = "ex")
    public void handleError(Throwable ex) {
        log.error("Error has occurred", ex);
        executorService.submit(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(recipients.toArray(new String[]{}));
                message.setSubject("Service error");
                message.setSentDate(new Date());

                String content = new BufferedReader(new InputStreamReader(messageTpl.getInputStream()))
                        .lines()
                        .collect(Collectors.joining("\n"));

                String zipkinTraceUrl = makeZipkinTraceUrl();
                message.setText(format(content, zipkinTraceUrl, Arrays.toString(ex.getStackTrace())));
                mailSender.send(message);
            } catch (Throwable th) {
                log.error("Error while send mail", th);
                throw new RuntimeException(th);
            }
        });
    }

    private String makeZipkinTraceUrl() {
        Span span = tracer.getCurrentSpan();
        if(span != null) {
            List<ServiceInstance> instances = discoveryClient.getInstances(ZIPKIN_SERVICE_ID);
            if (!instances.isEmpty()) {
                ServiceInstance zipkin = instances.get(0);
                return format("http://{0}:{1}/traces/{2}", zipkin.getHost(), zipkin.getPort(), String.valueOf(span.getTraceId()));
            }
        }
        return "";
    }
}
