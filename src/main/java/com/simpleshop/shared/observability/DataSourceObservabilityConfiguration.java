package com.simpleshop.shared.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class DataSourceObservabilityConfiguration {

    @Bean
    static DataSourceTracingPostProcessor dataSourceTracingPostProcessor(OpenTelemetry openTelemetry) {
        return new DataSourceTracingPostProcessor(openTelemetry);
    }

    static class DataSourceTracingPostProcessor implements BeanPostProcessor, Ordered {

        private final JdbcTelemetry jdbcTelemetry;

        DataSourceTracingPostProcessor(OpenTelemetry openTelemetry) {
            this.jdbcTelemetry = JdbcTelemetry.create(openTelemetry);
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof DataSource dataSource) {
                return jdbcTelemetry.wrap(dataSource);
            }
            return bean;
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
