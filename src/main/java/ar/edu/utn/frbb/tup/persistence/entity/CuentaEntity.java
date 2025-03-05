package ar.edu.utn.frbb.tup.persistence.entity;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;

import java.time.LocalDateTime;

/**
 * Representa una cuenta dentro de la capa de persistencia (similar a Cuenta, pero apta para almacenar en la base).
 * Se encarga de mapear los campos que se usarán para guardar y leer los datos de la cuenta.
 */
public class CuentaEntity extends BaseEntity {

    private String nombre;
    private LocalDateTime fechaCreacion;
    private int balance;
    private String tipoCuenta;
    private Long titular;
    private long numeroCuenta;
    private String tipoMoneda;

    /**
     * Constructor que toma un objeto de la capa de negocio (Cuenta) y
     * lo traduce a un objeto de persistencia (CuentaEntity).
     *
     * @param cuenta Objeto de negocio Cuenta a persistir.
     */
    public CuentaEntity(Cuenta cuenta) {
        super(cuenta.getNumeroCuenta());
        this.balance = cuenta.getBalance();
        this.tipoCuenta = cuenta.getTipoCuenta().toString();
        this.titular = cuenta.getTitular().getDni();
        this.fechaCreacion = cuenta.getFechaCreacion();
        this.numeroCuenta = cuenta.getNumeroCuenta();
        this.tipoMoneda = cuenta.getMoneda().toString();
    }

    /**
     * Método que permite reconstruir un objeto de negocio Cuenta
     * a partir de los datos almacenados en CuentaEntity.
     *
     * @return un objeto Cuenta que contiene los datos relevantes.
     */
    public Cuenta toCuenta() {
        Cuenta cuenta = new Cuenta();
        cuenta.setBalance(this.balance);
        cuenta.setNumeroCuenta(this.numeroCuenta);
        cuenta.setTipoCuenta(TipoCuenta.valueOf(this.tipoCuenta));
        cuenta.setFechaCreacion(this.fechaCreacion);
        cuenta.setMoneda(TipoMoneda.valueOf(this.tipoMoneda));
        // Asigna el DNI del titular en la cuenta para poder relacionarlo con un objeto Cliente a futuro.
        cuenta.setDniTitular(this.titular);

        return cuenta;
    }

    // Métodos getters y setters para manipular los campos privados.

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public Long getTitular() {
        return titular;
    }

    public void setTitular(Long titular) {
        this.titular = titular;
    }

    public long getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(long numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getTipoMoneda() {
        return tipoMoneda;
    }

    public void setTipoMoneda(String tipoMoneda) {
        this.tipoMoneda = tipoMoneda;
    }
}