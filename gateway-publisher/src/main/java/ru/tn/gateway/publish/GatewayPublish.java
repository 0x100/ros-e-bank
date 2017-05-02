package ru.tn.gateway.publish;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GatewayPublish {
    String path();
    String url();
}
