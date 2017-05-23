package org.springframework.cloud.netflix.feign;

import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.tn.broker.annotation.FeignClientAnnotation;
import ru.tn.broker.annotation.RequestMappingAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class FeignClientHelper {

    private static final String ANNOTATION_DATA = "annotationData";
    private static final String ANNOTATIONS = "annotations";
    private static final String DECLARED_ANNOTATIONS = "declaredAnnotations";
    private static final String MODIFIERS = "modifiers";
    private static final String PRIVATE_GET_DECLARED_METHODS_METHOD_NAME = "privateGetDeclaredMethods";

    private static final String GET = "get";
    private static final String UPDATE = "update";
    private static final String DELETE = "delete";

    @SneakyThrows
    public static <T> void setFeignClientAnnotations(String microServiceName, String microServiceUrl, Class<T> feignClientClass) {
        setClassAnnotation(microServiceName, feignClientClass);
        setMethodsAnnotations(microServiceUrl, feignClientClass);
    }

    private static <T> void setClassAnnotation(String microServiceName, Class<T> feignClientClass) {
        FeignClientAnnotation classAnnotation = new FeignClientAnnotation(microServiceName);
        addFeignClientAnnotation(feignClientClass, classAnnotation);
    }

    @SneakyThrows
    private static <T> void setMethodsAnnotations(String serviceUrl, Class<T> feignClientClass) {
        Method privateMethod = Class.class.getDeclaredMethod(PRIVATE_GET_DECLARED_METHODS_METHOD_NAME, boolean.class);
        privateMethod.setAccessible(true);

        Method[] methods = (Method[]) privateMethod.invoke(feignClientClass, false);
        for (Method method : methods) {
            addRequestMappingMethodAnnotation(method, serviceUrl);
        }
    }

    @SneakyThrows
    private static <T> void addFeignClientAnnotation(Class<T> feignClientClass, Annotation annotation) {
        Method annotationDataMethod = Class.class.getDeclaredMethod(ANNOTATION_DATA);
        annotationDataMethod.setAccessible(true);
        Object annotationData = annotationDataMethod.invoke(feignClientClass);

        Field annotationsField = annotationData.getClass().getDeclaredField(DECLARED_ANNOTATIONS);
        annotationsField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField(MODIFIERS);
        modifiersField.setAccessible(true);
        modifiersField.setInt(annotationsField, annotationsField.getModifiers() & ~Modifier.FINAL);

        Map annotations = new HashMap<>();
        annotationsField.set(annotationData, annotations);
        annotations.put(FeignClient.class, annotation);
    }

    @SneakyThrows
    private static void addRequestMappingMethodAnnotation(Method method, String serviceUrl) {
        RequestMethod httpMethod = getHttpMethod(method.getName());
        RequestMappingAnnotation annotation = new RequestMappingAnnotation(serviceUrl, httpMethod);

        Field declaredAnnotationsField = Executable.class.getDeclaredField(DECLARED_ANNOTATIONS);
        declaredAnnotationsField.setAccessible(true);

        Map annotations = new HashMap<>();
        declaredAnnotationsField.set(method, annotations);
        annotations.put(RequestMapping.class, annotation);
    }

    private static RequestMethod getHttpMethod(String methodName) {
        RequestMethod httpMethod;
        if (methodName.startsWith(GET)) {
            httpMethod = RequestMethod.GET;
        } else if (methodName.startsWith(UPDATE)) {
            httpMethod = RequestMethod.PUT;
        } else if (methodName.startsWith(DELETE)) {
            httpMethod = RequestMethod.DELETE;
        } else {
            httpMethod = RequestMethod.POST;
        }
        return httpMethod;
    }
}
