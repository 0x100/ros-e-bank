package org.springframework.cloud.netflix.feign;

import lombok.SneakyThrows;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import ru.tn.broker.utils.FeignClientInterfaceGenerator;

import java.text.MessageFormat;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DynamicFeignClient {

    @SneakyThrows
    public <T> T create(String microServiceName, String serviceUrl, Class<T> feignClientClass, ConfigurableApplicationContext context) {
        String beanName = MessageFormat.format("{0}-feign-client", microServiceName);

        Map<String, T> beanNames = context.getBeansOfType(feignClientClass);
        if (beanNames.containsKey(beanName)) {
            return (T) context.getBean(beanName);
        }
        Class newFeignClientInterface = FeignClientInterfaceGenerator.newFeignClientInterface(beanName, feignClientClass, microServiceName, serviceUrl);
        AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(FeignClientFactoryBean.class)
                .addPropertyValue("name", microServiceName)
                .addPropertyValue("url", "")
                .addPropertyValue("path", "")
                .addPropertyValue("type", newFeignClientInterface.getName())
                .addPropertyValue("decode404", false)
                .getBeanDefinition();

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
        registry.registerBeanDefinition(beanName, definition);

        return (T) context.getBean(beanName);
    }
}
