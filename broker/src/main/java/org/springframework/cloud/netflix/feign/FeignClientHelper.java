package org.springframework.cloud.netflix.feign;

import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    private static final List<String> SKIP_METHODS = Arrays.asList("equals", "hashCode", "toString");

    @SneakyThrows
    public static <T> void setFeignClientAnnotations(T feignClient, String microServiceName, String microServiceUrl) {
        Class<?> feignClientClass = feignClient.getClass();
        setClassAnnotation(feignClient, microServiceName);
        setMethodsAnnotations(microServiceUrl, feignClientClass);
    }

    private static <T> void setClassAnnotation(T feignClient, String microServiceName) {
        FeignClientAnnotation classAnnotation = new FeignClientAnnotation(microServiceName);
        addFeignClientAnnotation(feignClient, classAnnotation);
    }

    private static void setMethodsAnnotations(String microServiceUrl, Class<?> feignClientClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method privateGetDeclaredMethodsMethod = Class.class.getDeclaredMethod(PRIVATE_GET_DECLARED_METHODS_METHOD_NAME, boolean.class);
        privateGetDeclaredMethodsMethod.setAccessible(true);

        Method[] declaredMethods = (Method[]) privateGetDeclaredMethodsMethod.invoke(feignClientClass, false);
        for (Method method : declaredMethods) {

            String methodName = method.getName();
            if(SKIP_METHODS.contains(methodName))
                continue;

            RequestMethod httpMethod = getHttpMethod(methodName);
            RequestMappingAnnotation methodAnnotation = new RequestMappingAnnotation(microServiceUrl, httpMethod);
            addRequestMappingMethodAnnotation(method, methodAnnotation);
        }
    }

    @SneakyThrows
    private static <T> void addFeignClientAnnotation(T feignClient, Annotation annotation) {
        Method annotationDataMethod = Class.class.getDeclaredMethod(ANNOTATION_DATA);
        annotationDataMethod.setAccessible(true);
        Object annotationData = annotationDataMethod.invoke(feignClient.getClass());

        Field annotationsField = annotationData.getClass().getDeclaredField(ANNOTATIONS);
        annotationsField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField(MODIFIERS);
        modifiersField.setAccessible(true);
        modifiersField.setInt(annotationsField, annotationsField.getModifiers() & ~Modifier.FINAL);

        Map annotations = new HashMap<>();
        annotationsField.set(annotationData, annotations);
        annotations.put(FeignClient.class, annotation);
    }

    @SneakyThrows
    private static void addRequestMappingMethodAnnotation(Method targetMethod, Annotation annotation) {
        Field declaredAnnotationsField = Executable.class.getDeclaredField(DECLARED_ANNOTATIONS);
        declaredAnnotationsField.setAccessible(true);

        Map annotations = new HashMap<>();
        declaredAnnotationsField.set(targetMethod, annotations);
        annotations.put(RequestMapping.class, annotation);
    }

    private static RequestMethod getHttpMethod(String methodName) {
        RequestMethod httpMethod;
        if (methodName.startsWith(GET)) {
            httpMethod = RequestMethod.GET;
        }
        else if (methodName.startsWith(UPDATE)) {
            httpMethod = RequestMethod.PUT;
        }
        else if (methodName.startsWith(DELETE)) {
            httpMethod = RequestMethod.DELETE;
        }
        else {
            httpMethod = RequestMethod.POST;
        }
        return httpMethod;
    }
}
