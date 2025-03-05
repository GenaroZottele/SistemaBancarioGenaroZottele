package ar.edu.utn.frbb.tup.presentation.dto;

public class CuentaDto {
    private ClienteDto titular;
    private ClienteDto dni;
    private String tipoCuenta;
    private String moneda;

    public ClienteDto getTitular() {
        return titular;
    }

    public void setTitular(ClienteDto titular) {
        this.titular = titular;
    }

    public ClienteDto getDni() {
        return dni;
    }

    public void setDni(ClienteDto dni) {
        this.dni = dni;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
}