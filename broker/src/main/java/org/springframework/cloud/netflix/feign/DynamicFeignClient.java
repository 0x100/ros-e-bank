package org.springframework.cloud.netflix.feign;

import lombok.SneakyThrows;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import ru.tn.broker.utils.ClassGenerator;

import java.text.MessageFormat;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DynamicFeignClient {

    private final ClassGenerator proxyGenerator = new ClassGenerator();

    @SneakyThrows
    public <T> T create(String microServiceName, String serviceUrl, Class<T> feignClientClass, ConfigurableApplicationContext context) {
        String name = MessageFormat.format("{0}-feign-client", microServiceName);

        Map<String, T> beans = context.getBeansOfType(feignClientClass);
        if (beans.containsKey(name)) {
            return beans.get(name);
        }
//        Class proxyClass = proxyGenerator.getProxyClass(name, feignClientClass);
        FeignClientHelper.setFeignClientAnnotations(microServiceName, serviceUrl, feignClientClass);
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
}
