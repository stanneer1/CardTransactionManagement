package com.cardtransaction.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MetricsConfig {

    public MetricsConfig(io.micrometer.core.instrument.MeterRegistry registry,
                         @Value("${spring.application.name:card-transaction-management}") String appName) {
        // Set common tags
        registry.config().commonTags("application", appName);

        // Configure distribution statistics (SLA buckets and percentiles) for http.server.requests
        DistributionStatisticConfig httpRequestConfig = DistributionStatisticConfig.builder()
                .serviceLevelObjectives(Duration.ofMillis(200).toNanos(), Duration.ofMillis(500).toNanos(), Duration.ofSeconds(1).toNanos())
                .percentiles(0.5, 0.95)
                .percentilesHistogram(true)
                .build()
                .merge(DistributionStatisticConfig.DEFAULT);

        registry.config().meterFilter(new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(io.micrometer.core.instrument.Meter.Id id, DistributionStatisticConfig config) {
                if ("http.server.requests".equals(id.getName())) {
                    return httpRequestConfig.merge(config);
                }
                return config;
            }
        });
    }
}

