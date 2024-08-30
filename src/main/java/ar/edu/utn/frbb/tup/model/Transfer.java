package ar.edu.utn.frbb.tup.model;

public class Transfer {
    private int id;
    private long origen;
    private long destino;
    private double monto;
    private String descripcion;


    public Transfer() {
    }

    public Transfer(int id, long origen, long destino, double monto, String descripcion) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public Transfer setId(int id) {
        this.id = id;
        return this;
    }

    public long getOrigen() {
        return origen;
    }

    public Transfer setOrigin(int origen) {
        this.origen = origen;
        return this;
    }

    public long getDestino() {
        return destino;
    }

    public Transfer setDestino(long destino) {
        this.destino = destino;
        return this;
    }

    public double getMonto() {
        return monto;
    }

    public Transfer setAmount(double monto) {
        this.monto = monto;
        return this;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Transfer setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }
}
