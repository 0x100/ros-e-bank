package org.springframework.cloud.netflix.feign;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;

@SuppressWarnings("unchecked")
@Slf4j
public class DynamicFeignClient {

    private static final String ANNOTATION_DATA = "annotationData";
    private static final String ANNOTATIONS = "annotations";

    public <T> T create(String microServiceName, String serviceUrl, Class<T> feignClientClass, ConfigurableApplicationContext context) {
        String name = MessageFormat.format("{0}-feign-client", microServiceName);

        Map<String, T> beans = context.getBeansOfType(feignClientClass);
        if(beans.containsKey(name)) {
            return beans.get(name);
        }
        return registerFeignClientBean(microServiceName, feignClientClass, context, name);
    }

    private <T> T registerFeignClientBean(String microServiceName, Class<T> feignClientClass, ConfigurableApplicationContext context, String name) {
        AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(FeignClientFactoryBean.class)
                .addPropertyValue("name", microServiceName)
                .addPropertyValue("url", "")
                .addPropertyValue("path", "")
                .addPropertyValue("type", feignClientClass.getName())
                .addPropertyValue("decode404", false)
                .getBeanDefinition();

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
        registry.registerBeanDefinition(name, definition);

        return (T) context.getBean(name);
    }

    /**
     * Updates dynamically a FeignClient annotation's value for REST Docs aggregator
     */
    public static <T> void updateFeignClientAnnotationValue(String microServiceName, Class<T> feignClientClass) {
        Class<?> feignClient = feignClientClass.getInterfaces()[0];
        Annotation[] feignClientAnnotations = feignClient.getAnnotations();
        Annotation sourceAnnotation = feignClientAnnotations[0];
        FeignClientAnnotation changedAnnotation = new FeignClientAnnotation(microServiceName, sourceAnnotation);
        updateAnnotationValue(feignClientClass, changedAnnotation);
    }

    @SneakyThrows
    private static <T> void updateAnnotationValue(Class<T> feignClientClass, Annotation changedAnnotation) {
        Method annotationDataMethod = Class.class.getDeclaredMethod(ANNOTATION_DATA);
        annotationDataMethod.setAccessible(true);
        Object annotationData = annotationDataMethod.invoke(feignClientClass);

        Field annotationsField = annotationData.getClass().getDeclaredField(ANNOTATIONS);
        annotationsField.setAccessible(true);

        Map annotations = (Map) annotationsField.get(annotationData);
        annotations.put(FeignClient.class, changedAnnotation);
    }
}
