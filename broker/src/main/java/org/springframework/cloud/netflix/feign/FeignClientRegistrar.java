package org.springframework.cloud.netflix.feign;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.text.MessageFormat;
import java.util.Map;

public class FeignClientRegistrar {

    public <T> T create(String name, String url, String path, Class<T> type, ConfigurableApplicationContext context) {
        name = MessageFormat.format("{0}-feign-client", name);

        Map<String, T> beans = context.getBeansOfType(type);
        if(beans.containsKey(name)) {
            return beans.get(name);
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FeignClientFactoryBean.class);
        builder.addPropertyValue("name", name);
        builder.addPropertyValue("url", url);
        builder.addPropertyValue("path", path);
        builder.addPropertyValue("type", type.getName());
        builder.addPropertyValue("decode404", false);

        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) context.getBeanFactory();
        factory.registerBeanDefinition(name, builder.getBeanDefinition());
        context.refresh(); //FIXME GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once

        return context.getBean(type, name);
    }
}
