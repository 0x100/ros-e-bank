package ru.tn.gateway.publish.config;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;
import ru.tn.gateway.publish.annotation.EnableGatewayPublishing;
import ru.tn.gateway.publish.annotation.GatewayService;

import java.util.Map;

@Configuration
public class GatewayPublisherConfiguration {
    private static final String GATEWAY_SERVICE_KEY = "gateway/service/";

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private ConsulClient consulClient;

    @Autowired
    private ApplicationContext ctx;

    @EventListener(ContextRefreshedEvent.class)
    public void contextCreateEventListener() {
        Map<String, Object> beans = ctx.getBeansWithAnnotation(EnableGatewayPublishing.class);
        beans.values().forEach(bean -> {
            Class<?> beanClass = ClassUtils.isCglibProxy(bean) ? bean.getClass().getSuperclass() : bean.getClass();
                EnableGatewayPublishing annotation = beanClass.getAnnotation(EnableGatewayPublishing.class);
                if(annotation != null) {
                    String key = GATEWAY_SERVICE_KEY + appName;
                    GatewayService[] gatewayServices = annotation.value();
                    for (GatewayService gatewayService : gatewayServices) {
                        consulClient.setKVValue(key + "/url", gatewayService.url());
                        consulClient.setKVValue(key + "/path", gatewayService.path());
                    }
                }
        });
    }

}
