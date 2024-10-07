package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.dtos.RetiroDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface HeladerasRetrofitClient {
    @POST("/retiros")
    Call<Void> retirar(RetiroDTO retiro);

    @POST("/depositos")
    Call<Void> depositar(Integer heladeraID, String qrVianda);


}
