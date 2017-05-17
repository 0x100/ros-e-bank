package org.springframework.cloud.netflix.feign;

import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@SuppressWarnings("unchecked")
public class FeignClientHelper {

    private static final String ANNOTATION_DATA = "annotationData";
    private static final String ANNOTATIONS = "annotations";

    /**
     * Updates dynamically a FeignClient annotation's value for REST Docs aggregator
     */
    public static <T> void updateFeignClientAnnotationValue(String microServiceName, Class<T> feignClientClass) {
        Class<?> feignClient = feignClientClass.getInterfaces()[0];
        Annotation[] feignClientAnnotations = feignClient.getAnnotations();
        Annotation sourceAnnotation = feignClientAnnotations[0];
        FeignClientAnnotation changedAnnotation = new FeignClientAnnotation(microServiceName, sourceAnnotation);
        updateClassAnnotationValue(feignClientClass, changedAnnotation);
    }

    /**
     * Updates dynamically a FeignClient annotation's value for REST Docs aggregator
     */
    @SneakyThrows
    private static <T> void updateClassAnnotationValue(Class<T> feignClientClass, Annotation changedAnnotation) {
        Method annotationDataMethod = Class.class.getDeclaredMethod(ANNOTATION_DATA);
        annotationDataMethod.setAccessible(true);
        Object annotationData = annotationDataMethod.invoke(feignClientClass);

        Field annotationsField = annotationData.getClass().getDeclaredField(ANNOTATIONS);
        annotationsField.setAccessible(true);

        Map annotations = (Map) annotationsField.get(annotationData);
        annotations.put(FeignClient.class, changedAnnotation);
    }
}
