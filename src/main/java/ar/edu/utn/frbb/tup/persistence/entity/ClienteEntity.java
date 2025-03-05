package ar.edu.utn.frbb.tup.persistence.entity;

import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoPersona;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * La clase ClienteEntity es la representación de un Cliente dentro de la capa de persistencia.
 * Se utiliza para almacenar los datos necesarios y luego reconstruir un objeto de negocio (Cliente).
 */
public class ClienteEntity extends BaseEntity {

    private final String tipoPersona;
    private final String nombre;
    private final String apellido;
    private final LocalDate fechaAlta;
    private final LocalDate fechaNacimiento;
    private final String banco;
    private List<Long> cuentas;

    /**
     * Constructor que convierte un objeto Cliente de la capa de negocio a un ClienteEntity.
     * Se copian los campos relevantes y, en caso de tener cuentas, se guardan sus IDs.
     *
     * @param cliente Objeto de la capa de negocio que queremos persistir.
     */
    public ClienteEntity(Cliente cliente) {
        super(cliente.getDni());
        // Se almacena la descripción del TipoPersona para facilitar la persistencia.
        this.tipoPersona = cliente.getTipoPersona().getDescripcion();
        this.nombre = cliente.getNombre();
        this.apellido = cliente.getApellido();
        this.fechaAlta = cliente.getFechaAlta();
        this.fechaNacimiento = cliente.getFechaNacimiento();
        this.banco = cliente.getBanco();
        this.cuentas = new ArrayList<>();
        // En caso de que existan cuentas, se extraen sus números y se agregan a la lista.
        if (cliente.getCuentas() != null && !cliente.getCuentas().isEmpty()) {
            for (Cuenta c : cliente.getCuentas()) {
                cuentas.add(c.getNumeroCuenta());
            }
        }
    }

    /**
     * Método que reconstruye un objeto Cliente a partir de este ClienteEntity.
     * No se rellenan las cuentas aquí, ya que se manejan en otro nivel.
     *
     * @return un nuevo objeto Cliente con los datos básicos.
     */
    public Cliente toCliente() {
        Cliente cliente = new Cliente();
        // Se asigna el DNI a través del id de la clase BaseEntity.
        cliente.setDni(this.getId());
        cliente.setNombre(this.nombre);
        cliente.setApellido(this.apellido);
        // Se convierte el string tipoPersona al enum correspondiente.
        cliente.setTipoPersona(TipoPersona.fromString(this.tipoPersona));
        cliente.setFechaAlta(this.fechaAlta);
        cliente.setFechaNacimiento(this.fechaNacimiento);
        cliente.setBanco(this.banco);
        return cliente;
    }
}