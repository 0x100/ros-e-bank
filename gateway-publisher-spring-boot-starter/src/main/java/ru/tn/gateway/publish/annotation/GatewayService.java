package ru.tn.gateway.publish.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GatewayService {
    String path();
    String url();
}
