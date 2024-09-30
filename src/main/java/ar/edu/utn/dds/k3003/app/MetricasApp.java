package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.Service.ApprovalException;
import ar.edu.utn.dds.k3003.Service.DDMetricsUtils;
import ar.edu.utn.dds.k3003.Service.TransferDTO;
import io.javalin.Javalin;
import io.javalin.micrometer.MicrometerPlugin;
import io.javalin.http.HttpStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.datadog.DatadogMeterRegistry;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class MetricasApp {

    public static AtomicInteger metricaTrasladosEnCurso;
    public static Counter contadorTrasladosRealizados;
    public static Counter contadorRutasCreadas; //prueba
    public static DatadogMeterRegistry registry;

    public static void inicializarMetricas(){
        final var metricsUtils = new DDMetricsUtils("Metricas");
        registry = metricsUtils.getRegistry();


        // Metricas
        metricaTrasladosEnCurso = registry.gauge("trasladosEnCurso", new AtomicInteger(0));
        contadorTrasladosRealizados = registry.counter("trasladosRealizados");
        contadorRutasCreadas = registry.counter("rutasCreadas");
    }
    public static void main(final String... args) {

        inicializarMetricas();
        // Config
        final var micrometerPlugin = new MicrometerPlugin(config -> config.registry = registry);

        final var javalinServer = Javalin.create(config -> {
            config.registerPlugin(micrometerPlugin);
        });

        javalinServer.get("/cantidadTrasladosEnCurso", ctx -> {
            int cantidadEnCurso = metricaTrasladosEnCurso.get();
            ctx.result("Cantidad de traslados en curso: " + cantidadEnCurso);
        });

        // Endpoint para obtener la cantidad de traslados realizados
        javalinServer.get("/cantidadTrasladosRealizados", ctx -> {
            double cantidadRealizados = contadorTrasladosRealizados.count();
            ctx.result("Cantidad de traslados realizados: " + cantidadRealizados);
        });

        javalinServer.get("/cantidadRutasCreadas", ctx -> {
            double cantidadRutasCreadas = contadorRutasCreadas.count();
            ctx.result("Cantidad de rutas creadas: " + cantidadRutasCreadas);
        });

        javalinServer.post("/transaction", ctx -> {
            var transferencia = ctx.bodyAsClass( TransferDTO.class);
            try {
                transferir(transferencia);
                registry.counter("Metricas","status","ok").increment();
                ctx.result("ok!");
            } catch  (ApprovalException ex) {
                registry.counter("Metricas","status","rejected").increment();
                ctx.result("no aprobada!").status(HttpStatus.NOT_ACCEPTABLE);
            } catch  (Exception ex) {
                registry.counter("Metricas","status","error").increment();
                ctx.result("error!").status(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });


        javalinServer.start(7070);
    }

    private static void transferir(TransferDTO transferencia) throws ApprovalException {
        if(transferencia.getDst().equals(transferencia.getSrc())) {
            throw new ApprovalException();
        }
        if(transferencia.getAmount() <= 0) {
            throw new IllegalArgumentException();
        }

    }
}
///123