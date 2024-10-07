package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import kotlin.PublishedApi;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ViandasRetrofitClient {
//
    @GET("viandas/{qr}")
    Call<ViandaDTO> get(@Path("qr") String qr);

    @PATCH("viandas/{qrVianda}")
    Call<ViandaDTO> modificarHeladeraVianda(@Path("qrVianda") String qrVianda, @Query("idHeladera") int idHeladera);

    @PATCH("viandas/{qr}")
        Call<ViandaDTO> modificarEstado(@Path("qr") String qrVianda, @Query("estado") EstadoViandaEnum nuevoEstado);
}


