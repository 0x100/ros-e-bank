package ru.tn.gateway.publish.config;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tn.gateway.publish.annotation.EnableGatewayPublishing;
import ru.tn.gateway.publish.annotation.GatewayService;

@Configuration
public class GatewayPublisherConfiguration {
    @Autowired
    private ConsulClient consulClient;

    @Bean
    public BeanPostProcessor gatewayPublishBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                Class<?> beanClass = bean.getClass();
                EnableGatewayPublishing enableAnnotation = beanClass.getAnnotation(EnableGatewayPublishing.class);
                if(enableAnnotation != null) {
                    GatewayService[] gatewayServices = enableAnnotation.value();
                    for (GatewayService gatewayService : gatewayServices) {

                    }
                }
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }
        };
    }
}
