package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.io.IOException;
import java.util.*;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ViandasProxy implements FachadaViandas {
//
    private final String endpoint;
    private final ViandasRetrofitClient service;
    private static ViandasProxy instancia = null;
    private FachadaViandas fachadaViandas;

    public ViandasProxy(ObjectMapper objectMapper) {

        Dotenv dotenv = Dotenv.load();
        this.endpoint = dotenv.get("URL_VIANDAS");

        var retrofit =
                new Retrofit.Builder()
                        .baseUrl(this.endpoint)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .build();

        this.service = retrofit.create(ViandasRetrofitClient.class);
    }

    @Override

    public ViandaDTO agregar(ViandaDTO viandaDTO) {

        return null;
    }

    @Override
    public ViandaDTO modificarEstado(String s, EstadoViandaEnum estadoViandaEnum){
       ViandaDTO viandaDto = this.buscarXQR(s);
       // Response<ViandaDTO> response = service.modificarEstado().execute();
        viandaDto.setEstado(estadoViandaEnum);
        System.out.println("paso la modificacion de estado");
       return viandaDto;
    }

    @Override
    public List<ViandaDTO> viandasDeColaborador(Long aLong, Integer integer, Integer integer1){
        return null;
    }

    @SneakyThrows
    @Override
    public ViandaDTO buscarXQR(String qr) throws NoSuchElementException {
        Response<ViandaDTO> execute = service.get(qr).execute();

        if (execute.isSuccessful()) {
            return execute.body();
        }
        if (execute.code() == HttpStatus.NOT_FOUND.getCode()) {
            throw new NoSuchElementException("no se encontro la vianda " + qr);
        }
        throw new RuntimeException("Error conectandose con el componente viandas");
    }

    @Override
    public void setHeladerasProxy(FachadaHeladeras fachadaHeladeras) {

    }

    @Override
    public boolean evaluarVencimiento(String s) throws NoSuchElementException {
       return true;
    }

    @Override
    public ViandaDTO modificarHeladera(String s, int i) {
        System.out.println("entro a modificar heladera");
        try {
            Response<ViandaDTO> response = service.modificarHeladeraVianda(s,i).execute();
            System.out.println("ejecuto el response");
            if (response.isSuccessful()) {
                return response.body();
            } else if (response.code() == HttpStatus.NOT_FOUND.getCode()) {
                throw new NoSuchElementException("No se encontró la vianda con el código QR: " + s);
            }else {
                throw new NoSuchElementException("no se pudo modificar la heladera de la vianda");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}