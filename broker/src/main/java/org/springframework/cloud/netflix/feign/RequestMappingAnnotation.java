package org.springframework.cloud.netflix.feign;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;

public class RequestMappingAnnotation implements RequestMapping {

    private String url;
    private Annotation annotation;

    RequestMappingAnnotation(String url, Annotation annotation) {
        this.url = url;
        this.annotation = annotation;
    }

    @Override
    public String name() {
        return url;
    }

    @Override
    public String[] value() {
        return new String[]{url};
    }

    @Override
    public String[] path() {
        return new String[0];
    }

    @Override
    public RequestMethod[] method() {
        return ((RequestMapping) annotation).method();
    }

    @Override
    public String[] params() {
        return new String[0];
    }

    @Override
    public String[] headers() {
        return new String[0];
    }

    @Override
    public String[] consumes() {
        return new String[0];
    }

    @Override
    public String[] produces() {
        return new String[0];
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return annotation.annotationType();
    }
}
