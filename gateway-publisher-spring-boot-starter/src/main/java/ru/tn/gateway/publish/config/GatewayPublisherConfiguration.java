package ru.tn.gateway.publish.config;

import com.ecwid.consul.v1.ConsulClient;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tn.gateway.publish.annotation.EnableGatewayPublishing;
import ru.tn.gateway.publish.annotation.GatewayService;

import java.util.Map;

@Slf4j
@Configuration
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
public class GatewayPublisherConfiguration {
    private static final String GATEWAY_SERVICE_KEY = "gateway/service/";
    private static final String GATEWAY_SERVICE_URL = "/url";
    private static final String GATEWAY_SERVICE_PATH = "/path";

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
                    consulClient.setKVValue(key + GATEWAY_SERVICE_URL, gatewayService.url());
                    consulClient.setKVValue(key + GATEWAY_SERVICE_PATH, gatewayService.path());
                }
            }
        });
        fillDependsServices();
    }

    @SneakyThrows
    private void fillDependsServices() {
        String[] beanNames = ctx.getBeanNamesForAnnotation(FeignClient.class);
        String key = GATEWAY_SERVICE_KEY + appName + "/dependency";
        if (beanNames != null && beanNames.length > 0)
            for (String beanName : beanNames) {
                BeanDefinition beanDefinition = ((GenericApplicationContext) ctx).getBeanDefinition(beanName);
                if (beanDefinition instanceof GenericBeanDefinition) {
                    String type = (String) beanDefinition.getPropertyValues().getPropertyValue("type").getValue();//todo check NullPointer
                    String serviceName = Class.forName(type).getAnnotation(FeignClient.class).value();
                    String serviceUrl = Class.forName(type).getDeclaredMethods()[0].getAnnotation(RequestMapping.class).value()[0];
                    consulClient.setKVValue(key + "/" + serviceName, serviceName + "|" + serviceUrl);
                }
            }
    }

    public static String getGatewayServiceUrlKey(String serviceName) {
        return GATEWAY_SERVICE_KEY + serviceName + GATEWAY_SERVICE_URL;
    }
}
