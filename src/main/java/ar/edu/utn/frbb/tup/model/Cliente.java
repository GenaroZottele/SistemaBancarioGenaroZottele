package ar.edu.utn.frbb.tup.model;

import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Cliente extends Persona {
    private TipoPersona tipoPersona;
    private String banco;
    
    // Opcional: formateamos la fecha
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaAlta;
    
    // La colección de cuentas se mantendrá sin anotaciones de nivel de clase para evitar omitirla
    private Set<Cuenta> cuentas = new HashSet<>();

    public Cliente() {
        super();
    }
    
    public Cliente(ClienteDto clienteDto) {
        super(clienteDto.getDni(), clienteDto.getApellido(), clienteDto.getNombre(), clienteDto.getFechaNacimiento());
        fechaAlta = LocalDate.now();
        banco = clienteDto.getBanco();
        tipoPersona = TipoPersona.fromString(clienteDto.getTipoPersona());
    }

    // Getters y setters

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

    @JsonManagedReference
    public Set<Cuenta> getCuentas() {
        return cuentas;
    }    

    // Este getter es opcional y se puede usar para evitar serializar cuentas si se desea.
    @JsonIgnore
    public Set<Cuenta> getCuentasParaSerializar() {
        return cuentas;
    }

    public void addCuenta(Cuenta cuenta) {
        this.cuentas.add(cuenta);
        cuenta.setTitular(this);
    }

    public boolean tieneCuenta(TipoCuenta tipoCuenta, TipoMoneda moneda) {
        for (Cuenta cuenta : cuentas) {
            if (tipoCuenta.equals(cuenta.getTipoCuenta()) && moneda.equals(cuenta.getMoneda())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "tipoPersona=" + tipoPersona +
                ", banco='" + banco + '\'' +
                ", fechaAlta=" + fechaAlta +
                ", cuentas=" + cuentas +
                '}';
    }
}
