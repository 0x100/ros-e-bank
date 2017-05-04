package ru.tn.gateway.publish.config;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import ru.tn.gateway.publish.annotation.EnableGatewayPublishing;
import ru.tn.gateway.publish.annotation.GatewayService;

@Configuration
public class GatewayPublisherConfiguration {
    private static final String GATEWAY_SERVICE_KEY = "gateway.service";
    private final ConsulClient consulClient;
    private final String appName;

    @Autowired
    public GatewayPublisherConfiguration(ConsulClient consulClient, @Value("${spring.application.name}") String appName) {
        this.consulClient = consulClient;
        this.appName = appName;
    }

    @Bean
    public BeanPostProcessor gatewayPublishBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                Class<?> beanClass = ClassUtils.isCglibProxy(bean) ? bean.getClass().getSuperclass() : bean.getClass();
                EnableGatewayPublishing annotation = beanClass.getAnnotation(EnableGatewayPublishing.class);
                if(annotation != null) {
                    String key = GATEWAY_SERVICE_KEY + "." + appName;
                    GatewayService[] gatewayServices = annotation.value();
                    for (GatewayService gatewayService : gatewayServices) {
                        consulClient.setKVValue(key + ".url", gatewayService.url());
                        consulClient.setKVValue(key + ".path", gatewayService.path());
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
