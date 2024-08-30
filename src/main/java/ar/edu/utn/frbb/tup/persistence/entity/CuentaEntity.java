package ar.edu.utn.frbb.tup.persistence.entity;

import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;

import java.time.LocalDateTime;

public class CuentaEntity extends BaseEntity{
    String nombre;
    LocalDateTime fechaCreacion;
    int balance;
    String tipoCuenta;
    Long titular;
    long CVU;

    public CuentaEntity(Cuenta cuenta) {
        super(cuenta.getCVU());
        this.balance = cuenta.getBalance();
        this.tipoCuenta = cuenta.getTipoCuenta().toString();
        this.titular = cuenta.getTitular().getDni();
        this.fechaCreacion = cuenta.getFechaCreacion();
    }

    public Cuenta toCuenta() {
        Cuenta cuenta = new Cuenta();
        cuenta.setBalance(this.balance);
        cuenta.setCVU(this.CVU);
        cuenta.setTipoCuenta(TipoCuenta.valueOf(this.tipoCuenta));
        cuenta.setFechaCreacion(this.fechaCreacion);
        return cuenta;
    }

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

    public long getCVU() {
        return CVU;
    }

    public void setCVU(long CVU) {
        this.CVU = CVU;
    }
}
