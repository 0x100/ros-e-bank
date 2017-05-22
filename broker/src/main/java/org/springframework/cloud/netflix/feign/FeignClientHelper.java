package org.springframework.cloud.netflix.feign;

import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@SuppressWarnings("unchecked")
public class FeignClientHelper {

    private static final String ANNOTATION_DATA = "annotationData";
    private static final String ANNOTATIONS = "annotations";
    private static final String DECLARED_ANNOTATIONS = "declaredAnnotations";

    /**
     * Dynamically updates FeignClient's annotations for REST Docs aggregator
     */
    public static <T> void updateFeignClientAnnotations(String microServiceName, String microServiceUrl, Class<T> feignClientClass) {
        Class<?> feignClient = feignClientClass.getInterfaces()[0];
        FeignClientAnnotation changedClassAnnotation = new FeignClientAnnotation(microServiceName, feignClient.getAnnotations()[0]);
        replaceClassAnnotation(feignClientClass, changedClassAnnotation);

        Method[] declaredMethods = feignClient.getDeclaredMethods();
        for (Method method : declaredMethods) {
            RequestMappingAnnotation changedMethodAnnotation = new RequestMappingAnnotation(microServiceUrl, method.getAnnotations()[0]);
            replaceMethodAnnotation(feignClientClass, method, changedMethodAnnotation);
        }
    }

    /**
     * Dynamically updates FeignClient's annotations for REST Docs aggregator
     */
    @SneakyThrows
    private static <T> void replaceClassAnnotation(Class<T> feignClientClass, Annotation changedAnnotation) {
        Method annotationDataMethod = Class.class.getDeclaredMethod(ANNOTATION_DATA);
        annotationDataMethod.setAccessible(true);
        Object annotationData = annotationDataMethod.invoke(feignClientClass);

        Field annotationsField = annotationData.getClass().getDeclaredField(ANNOTATIONS);
        annotationsField.setAccessible(true);

        Map annotations = (Map) annotationsField.get(annotationData);
        annotations.put(FeignClient.class, changedAnnotation);
    }

    /**
     * Dynamically updates FeignClient's method annotations
     */
    @SneakyThrows
    private static <T> void replaceMethodAnnotation(Class<T> feignClientClass, Method targetMethod, Annotation changedAnnotation) {
        Field declaredAnnotationsField = feignClientClass.getClass().getDeclaredField(DECLARED_ANNOTATIONS);
        declaredAnnotationsField.setAccessible(true);
        Map annotations = (Map) declaredAnnotationsField.get(targetMethod);
        annotations.put(RequestMapping.class, changedAnnotation);
    }
}
