package ar.edu.utn.dds.k3003.Service;

import java.time.Duration;

import io.github.cdimascio.dotenv.Dotenv;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import lombok.Getter;

public class DDMetricsUtils {
    @Getter
    private final DatadogMeterRegistry registry;

    public DDMetricsUtils(String appTag) {

        Dotenv dotenv = Dotenv.load();
        var config = new DatadogConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            public String apiKey() {
                return dotenv.get("DDAPIKEY");
            }

            @Override
            public String uri() {
                return "https://api.us5.datadoghq.com";
            }

            @Override
            public String get(String k) {
                return null; // aceptar la configuración por defecto
            }
        };
        registry = new DatadogMeterRegistry(config, Clock.SYSTEM);
        registry.config().commonTags("app", appTag);
        initInfraMonitoring();
    }

    private void initInfraMonitoring() {
        try (var jvmGcMetrics = new JvmGcMetrics();
             var jvmHeapPressureMetrics = new JvmHeapPressureMetrics()) {
            jvmGcMetrics.bindTo(registry);
            jvmHeapPressureMetrics.bindTo(registry);
        }
        new JvmMemoryMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new FileDescriptorMetrics().bindTo(registry);
    }
}