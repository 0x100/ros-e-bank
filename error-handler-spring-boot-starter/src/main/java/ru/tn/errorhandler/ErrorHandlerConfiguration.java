package ru.tn.errorhandler;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "ru.tn.errorhandler")
@PropertySource("classpath:ru/tn/errorhandler/application.yaml")
public class ErrorHandlerConfiguration {
}
