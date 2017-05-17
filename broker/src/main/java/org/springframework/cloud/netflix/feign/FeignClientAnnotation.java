package org.springframework.cloud.netflix.feign;

import java.lang.annotation.Annotation;

public class FeignClientAnnotation implements FeignClient {

    private String name;
    private Annotation sourceAnnotation;

    FeignClientAnnotation(String name, Annotation sourceAnnotation) {
        this.name = name;
        this.sourceAnnotation = sourceAnnotation;
    }

    @Override
    public String value() {
        return name;
    }

    @Override
    public String serviceId() {
        return null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String qualifier() {
        return null;
    }

    @Override
    public String url() {
        return "";
    }

    @Override
    public boolean decode404() {
        return false;
    }

    @Override
    public Class<?>[] configuration() {
        return new Class[0];
    }

    @Override
    public Class<?> fallback() {
        return null;
    }

    @Override
    public Class<?> fallbackFactory() {
        return null;
    }

    @Override
    public String path() {
        return "";
    }

    @Override
    public boolean primary() {
        return true;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return sourceAnnotation.annotationType();
    }
}
