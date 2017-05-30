package ru.tn.bootstrap;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
public class BootstrapConfiguration {
}
