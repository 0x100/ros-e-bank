package ru.tn.gateway.publish.annotation;

import org.springframework.context.annotation.Import;
import ru.tn.gateway.publish.config.GatewayPublisherConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(GatewayPublisherConfiguration.class)
public @interface EnableGatewayPublishing {
    GatewayService[] value();
}
