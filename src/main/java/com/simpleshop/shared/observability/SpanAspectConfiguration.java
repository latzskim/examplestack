package com.simpleshop.shared.observability;

import io.micrometer.tracing.annotation.DefaultNewSpanParser;
import io.micrometer.tracing.annotation.ImperativeMethodInvocationProcessor;
import io.micrometer.tracing.annotation.NewSpanParser;
import io.micrometer.tracing.annotation.SpanAspect;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SpanAspectConfiguration {

    @Bean
    NewSpanParser newSpanParser() {
        return new DefaultNewSpanParser();
    }

    @Bean
    ImperativeMethodInvocationProcessor methodInvocationProcessor(
            NewSpanParser newSpanParser,
            Tracer tracer,
            BeanFactory beanFactory) {
        return new ImperativeMethodInvocationProcessor(
                newSpanParser,
                tracer,
                beanFactory::getBean,
                beanFactory::getBean
        );
    }

    @Bean
    SpanAspect spanAspect(ImperativeMethodInvocationProcessor methodInvocationProcessor) {
        return new SpanAspect(methodInvocationProcessor);
    }
}
