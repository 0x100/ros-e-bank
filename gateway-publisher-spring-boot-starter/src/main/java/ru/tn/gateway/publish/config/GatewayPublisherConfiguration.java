package ru.tn.gateway.publish.config;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tn.gateway.publish.annotation.EnableGatewayPublishing;
import ru.tn.gateway.publish.annotation.GatewayService;

import java.lang.reflect.Method;
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
            if (annotation != null) {
                String key = GATEWAY_SERVICE_KEY + appName;
                GatewayService[] gatewayServices = annotation.value();
                for (GatewayService gatewayService : gatewayServices) {
                    consulClient.setKVValue(key + "/url", gatewayService.url());
                    consulClient.setKVValue(key + "/path", gatewayService.path());
                }
            }
        });
        fillDependsServices();
    }

    public void fillDependsServices() {
        String[] beans = ctx.getBeanNamesForAnnotation(FeignClient.class);
        String key = GATEWAY_SERVICE_KEY + appName + "/dependency";
        for (String className : beans) {
            try {
                Class<?> aClass = Class.forName(className);
                String serviceId = aClass.getAnnotation(FeignClient.class).value();
                Method[] declaredMethods = aClass.getDeclaredMethods();
                for (Method method : declaredMethods) {
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    if (requestMapping != null) {
                        consulClient.setKVValue(key, serviceId + "|" + requestMapping.value()[0]);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}
