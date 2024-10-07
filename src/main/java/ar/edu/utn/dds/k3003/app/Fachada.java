package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.*;
import ar.edu.utn.dds.k3003.facades.exceptions.TrasladoNoAsignableException;
import ar.edu.utn.dds.k3003.model.Ruta;
import ar.edu.utn.dds.k3003.model.Traslado;
import ar.edu.utn.dds.k3003.repositories.*;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Getter
@Setter
public class Fachada implements ar.edu.utn.dds.k3003.facades.FachadaLogistica{

    private final RutaRepository rutaRepository;
    private final RutaMapper rutaMapper;
    private final TrasladoRepository trasladoRepository;
    private final TrasladoMapper trasladoMapper;
    private FachadaViandas fachadaViandas;
    private FachadaHeladeras fachadaHeladeras;

    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    public Fachada() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("entrega3_tp_dds");
        this.entityManager = entityManagerFactory.createEntityManager();
        this.rutaRepository = new RutaRepository(entityManager);
        this.rutaMapper = new RutaMapper();
        this.trasladoMapper = new TrasladoMapper();
        this.trasladoRepository = new TrasladoRepository(entityManager);
    }

    @Override
    public RutaDTO agregar(RutaDTO rutaDTO) {
        Ruta ruta = new Ruta(rutaDTO.getColaboradorId(), rutaDTO.getHeladeraIdOrigen(), rutaDTO.getHeladeraIdDestino());
        ruta = this.rutaRepository.save(ruta);
        return rutaMapper.map(ruta);
    }

    @Override
    public TrasladoDTO buscarXId(Long aLong) throws NoSuchElementException { //el traslado
        Traslado traslado = trasladoRepository.findById(aLong);

        TrasladoDTO trasDto = trasladoMapper.map(traslado);

        return trasDto;
    }

    @Override
    public TrasladoDTO asignarTraslado(TrasladoDTO trasladoDTO) throws TrasladoNoAsignableException {

        ViandaDTO viandaDTO = fachadaViandas.buscarXQR(trasladoDTO.getQrVianda());
        if (viandaDTO == null) {
            throw new TrasladoNoAsignableException("No se encontró la vianda con el QR proporcionado");
        }

        List<Ruta> rutasPosibles = this.rutaRepository.findByHeladeras(trasladoDTO.getHeladeraOrigen(), trasladoDTO.getHeladeraDestino());
        if (rutasPosibles == null || rutasPosibles.isEmpty()) {
            throw new TrasladoNoAsignableException("No se puede asignar traslado porque no hay una ruta");
        }

        Collections.shuffle(rutasPosibles);

        Ruta ruta = rutasPosibles.get(0);
        Traslado traslado = trasladoRepository.save(new Traslado(viandaDTO.getCodigoQR(), ruta,
                EstadoTrasladoEnum.ASIGNADO, trasladoDTO.getFechaTraslado()));



        return this.trasladoMapper.map(traslado);
    }


    @Override
    public List<TrasladoDTO> trasladosDeColaborador(Long aLong, Integer integer, Integer integer1) {
        List<Traslado> trasladosDeColaborador = this.trasladoRepository.findByColaboradorId(aLong,integer,integer);
        List<Traslado> trasladosPorMesYAnio = trasladosDeColaborador.stream()
                .filter(t -> t.getRuta().getColaboradorId().equals(aLong))
                .filter(x -> x.getFechaTraslado().getMonthValue() == integer)
                .filter(x -> x.getFechaTraslado().getYear() == integer1)
                .collect(Collectors.toList());


         List<TrasladoDTO> trasDtos = trasladosPorMesYAnio.stream().map(t -> trasladoMapper.map(t)).collect(Collectors.toList());

        return trasDtos;
    }

    @Override
    public void setHeladerasProxy(FachadaHeladeras fachadaHeladeras) {
            this.fachadaHeladeras = fachadaHeladeras;
    }

    @Override
    public void setViandasProxy(FachadaViandas fachadaViandas) {
        this.fachadaViandas = fachadaViandas;
    }

    @Override
    public void trasladoRetirado(Long aLong) {
        //primero busco el id del traslado
        TrasladoDTO trasladoDto = buscarXId(aLong);

        //genero el retiroDto
        //RetiroDTO retiroHeladera = new RetiroDTO(trasladoDto.getQrVianda(), "123", trasladoDto.getHeladeraOrigen());
        //fachadaHeladeras.retirar(retiroHeladera);

        //ahora cambio el estado de la vianda
        fachadaViandas.modificarEstado(trasladoDto.getQrVianda(), EstadoViandaEnum.EN_TRASLADO);
        this.trasladoRepository.modificarEstado(aLong,EstadoTrasladoEnum.EN_VIAJE);
    }
    @Override
    public void trasladoDepositado(Long aLong) {
        //es un traslado que ya fue creado en trasladoRetirado y ahora lo tengo que buscar
        TrasladoDTO trasladoDTO = buscarXId(aLong);

        //la deposito en la heladera
        fachadaHeladeras.depositar(trasladoDTO.getHeladeraDestino(), trasladoDTO.getQrVianda());

        //cambio el estado de la vianda
        fachadaViandas.modificarEstado(trasladoDTO.getQrVianda(), EstadoViandaEnum.DEPOSITADA);
        fachadaViandas.modificarHeladera(trasladoDTO.getQrVianda(), trasladoDTO.getHeladeraDestino());

        //cambio el estado del traslado
       this.trasladoRepository.modificarEstado(aLong, EstadoTrasladoEnum.ENTREGADO);

    }

    public List<Ruta> obtenerTodasLasRutas() {
       return this.rutaRepository.getRutas().stream().toList();
    }

    public void borrarTraslados(){

        this.trasladoRepository.borrarTraslados();
    }

    public void borrarRutas(){
        this.rutaRepository.borrarRutas();
    }
}
