package org.springframework.cloud.netflix.feign;

import lombok.SneakyThrows;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;

import java.text.MessageFormat;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DynamicFeignClient {

    @SneakyThrows
    public <T> String create(String microServiceName, String serviceUrl, Class<T> feignClientClass, ConfigurableApplicationContext context) {
        String beanName = MessageFormat.format("{0}-feign-client", microServiceName);

        Map<String, T> beanNames = context.getBeansOfType(feignClientClass);
        if (beanNames.containsKey(beanName)) {
            return beanName;
        }
        FeignClientHelper.setFeignClientAnnotations(microServiceName, serviceUrl, feignClientClass);
        AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(FeignClientFactoryBean.class)
                .addPropertyValue("name", microServiceName)
                .addPropertyValue("url", "")
                .addPropertyValue("path", "")
                .addPropertyValue("type", feignClientClass.getName())
                .addPropertyValue("decode404", false)
                .getBeanDefinition();

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
        registry.registerBeanDefinition(beanName, definition);

        return beanName;
    }
}
