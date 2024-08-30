package ar.edu.utn.frbb.tup.persistence.entity;

import java.time.LocalDateTime;

public class TransferEntity {
    private Long id;
    private Long origen;
    private Long destino;
    private Double monto;
    private LocalDateTime transferDate;
    private String descripcion;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrigen() {
        return origen;
    }

    public void setOrigen(Long origen) {
        this.origen = origen;
    }

    public Long getDestino() {
        return destino;
    }

    public void setDestino(Long destino) {
        this.destino = destino;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}