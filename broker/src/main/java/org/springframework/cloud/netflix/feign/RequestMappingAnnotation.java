package org.springframework.cloud.netflix.feign;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;

public class RequestMappingAnnotation implements RequestMapping {

    private String url;
    private RequestMethod httpMethod;

    RequestMappingAnnotation(String url, RequestMethod httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    @Override
    public String name() {
        return url;
    }

    @Override
    public String[] value() {
        return path();
    }

    @Override
    public String[] path() {
        return new String[]{url};
    }

    @Override
    public RequestMethod[] method() {
        return new RequestMethod[]{httpMethod};
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
        return RequestMapping.class;
    }
}
