package ar.edu.utn.frbb.tup.model;

import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cliente extends Persona{

    private TipoPersona tipoPersona;
    private String banco;
    private LocalDate fechaAlta;
    private Set<Cuenta> cuentas = new HashSet<>();

    public Cliente() {
        super();
    }
    public Cliente(ClienteDto clienteDto) {
        super(clienteDto.getDni(), clienteDto.getApellido(), clienteDto.getNombre(), clienteDto.getFechaNacimiento(), clienteDto.getDireccion());
        fechaAlta = LocalDate.now();
        banco = clienteDto.getBanco();
    }

    public TipoPersona getTipoPersona() {
        return tipoPersona;
    }

    public void setTipoPersona(TipoPersona tipoPersona) {
        this.tipoPersona = tipoPersona;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Set<Cuenta> getCuentas() {
        return cuentas;
    } //TODO: ver si se puede cambiar a List

    public void setCuentas(Set<Cuenta> cuentas) {
        this.cuentas = cuentas;
    }

    public void addCuenta(Cuenta cuenta) {
        this.cuentas.add(cuenta);
        cuenta.setTitular(this); //TODO: ver si se puede hacer en el constructor
    }

//    public boolean tieneCuenta(TipoCuenta tipoCuenta, TipoMoneda moneda) {
//        for (Cuenta cuenta:
//                cuentas) {
//            if (tipoCuenta.equals(cuenta.getTipoCuenta()) && moneda.equals(cuenta.getMoneda())) {
//                return true;
//            }
//        }
//        return false;
//    } ver si se puede trasladar a la capa de servicio

    @Override
    public String toString() {
        return "\n ///// Cliente ///// \n" +
                " dni: " + getDni() +
                "\n nombre: " + getNombre() +
                "\n apellido: " + getApellido() +
                "\n tipoPersona: " + getTipoPersona() +
                "\n banco: " + getBanco() +
                "\n fechaAlta: " + getFechaAlta() +
                "\n cuentas: " + getCuentas();
    }
}
