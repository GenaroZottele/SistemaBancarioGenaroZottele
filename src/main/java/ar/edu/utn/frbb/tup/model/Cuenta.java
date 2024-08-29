package ar.edu.utn.frbb.tup.model;

import ar.edu.utn.frbb.tup.model.exception.CantidadNegativaException;
import ar.edu.utn.frbb.tup.model.exception.NoAlcanzaException;
import ar.edu.utn.frbb.tup.presentation.dto.CuentaDto;

import java.time.LocalDateTime;
import java.util.Random;

public class Cuenta {
    private Cliente titular;
    private Cliente dni;
    private long CVU;
    private LocalDateTime fechaCreacion;
    private int balance;
    private TipoCuenta tipoCuenta;
    private TipoMoneda moneda;

    public Cuenta() {
        this.CVU = new Random().nextLong();// TESTEAR si anda
        this.balance = 0;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Cuenta(CuentaDto cuentaDto) {
        this.titular = new Cliente(cuentaDto.getTitular());
        this.dni = new Cliente(cuentaDto.getDni());
        this.tipoCuenta = TipoCuenta.valueOf(cuentaDto.getTipoCuenta());
        this.moneda = TipoMoneda.valueOf(cuentaDto.getMoneda());
        this.CVU = new Random().nextLong();// TESTEAR si anda
        this.balance = 0;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Cliente getTitular() {
        return titular;
    }

    public void setTitular(Cliente titular) {
        this.titular = titular;
    }


    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public Cuenta setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
        return this;
    }

    public TipoMoneda getMoneda() {
        return moneda;
    }

    public Cuenta setMoneda(TipoMoneda moneda) {
        this.moneda = moneda;
        return this;
    }


    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Cuenta setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        return this;
    }

    public int getBalance() {
        return balance;
    }

    public Cuenta setBalance(int balance) {
        this.balance = balance;
        return this;
    }

    public void debitarDeCuenta(int cantidadADebitar) throws NoAlcanzaException, CantidadNegativaException {
        if (cantidadADebitar < 0) {
            throw new CantidadNegativaException();
        }

        if (balance < cantidadADebitar) {
            throw new NoAlcanzaException();
        }
        this.balance = this.balance - cantidadADebitar;
    }

    public void setCVU(long CVU) {
        this.CVU = CVU;
    }

    public void forzaDebitoDeCuenta(int i) {
        this.balance = this.balance - i;
    }

    public long getCVU() {
        return CVU;
    }


}
