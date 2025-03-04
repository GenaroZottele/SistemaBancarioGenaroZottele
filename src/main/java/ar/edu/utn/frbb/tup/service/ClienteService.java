package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;
import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import org.springframework.stereotype.Service;

/**
 * Servicio que se encarga de la lógica de negocio relacionada con los clientes.
 * Incluye métodos para dar de alta un cliente, asignarle cuentas y buscarlo por DNI.
 */
@Service
public class ClienteService {

    // DAO que maneja las operaciones de persistencia de los objetos Cliente.
    ClienteDao clienteDao;

    /**
     * Constructor que inyecta el ClienteDao, permitiendo las operaciones en la base en memoria.
     *
     * @param clienteDao DAO para la persistencia de los clientes.
     */
    public ClienteService(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    /**
     * Da de alta un nuevo cliente en el sistema, validando que no exista previamente
     * y que cumpla con los requisitos (edad mínima, datos no nulos, etc.).
     *
     * @param clienteDto Datos provenientes de la capa de presentación.
     * @return Objeto Cliente creado.
     * @throws ClienteAlreadyExistsException si el cliente con el mismo DNI ya existe.
     */
    public Cliente darDeAltaCliente(ClienteDto clienteDto) throws ClienteAlreadyExistsException {

        // Validaciones de campos obligatorios y rangos válidos.
        if (clienteDto == null) {
            throw new IllegalArgumentException("El ClienteDto no puede ser nulo");
        }
        if (clienteDto.getDni() <= 0) {
            throw new IllegalArgumentException("El DNI del cliente debe ser mayor a 0");
        }
        if (clienteDto.getNombre() == null || clienteDto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        if (clienteDto.getApellido() == null || clienteDto.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del cliente no puede estar vacío");
        }
        if (clienteDto.getFechaNacimiento() == null || clienteDto.getFechaNacimiento().trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha de nacimiento del cliente no puede estar vacía");
        }

        // Se crea el objeto Cliente con los datos del DTO.
        Cliente cliente = new Cliente(clienteDto);

        // Verifica si el cliente ya existe con el mismo DNI.
        if (clienteDao.find(cliente.getDni(), false) != null) {
            throw new ClienteAlreadyExistsException("Ya existe un cliente con DNI " + cliente.getDni());
        }

        // Chequea si el cliente es mayor de 18 años.
        if (cliente.getEdad() < 18) {
            throw new IllegalArgumentException("El cliente debe ser mayor a 18 años");
        }

        // Guarda el nuevo cliente en la base en memoria.
        clienteDao.save(cliente);
        return cliente;
    }

    /**
     * Asocia una cuenta a un cliente existente, verificando que no haya una cuenta igual (mismo tipo y moneda).
     *
     * @param cuenta      Cuenta que se quiere asignar al cliente.
     * @param dniTitular  DNI del cliente titular de la cuenta.
     * @throws TipoCuentaAlreadyExistsException si el cliente ya posee una cuenta de ese tipo y moneda.
     */
    public void agregarCuenta(Cuenta cuenta, long dniTitular) throws TipoCuentaAlreadyExistsException {

        // Validaciones de cuenta y DNI del titular.
        if (cuenta == null) {
            throw new IllegalArgumentException("La cuenta no puede ser nula");
        }
        if (dniTitular <= 0) {
            throw new IllegalArgumentException("El DNI del titular debe ser mayor a 0");
        }

        // Se busca el cliente, lanzando excepción si no existe.
        Cliente titular = buscarClientePorDni(dniTitular);
        cuenta.setTitular(titular);

        // Verifica si ya existe otra cuenta con el mismo tipo y moneda.
        if (titular.tieneCuenta(cuenta.getTipoCuenta(), cuenta.getMoneda())) {
            throw new TipoCuentaAlreadyExistsException("El cliente ya posee una cuenta de ese tipo y moneda");
        }

        // Agrega la cuenta al cliente y persiste el cambio.
        titular.addCuenta(cuenta);
        clienteDao.save(titular);
    }

    /**
     * Busca un cliente en la base en memoria por su DNI.
     *
     * @param dni DNI del cliente buscado.
     * @return Objeto Cliente encontrado.
     * @throws IllegalArgumentException si el cliente no existe o el DNI es inválido.
     */
    public Cliente buscarClientePorDni(long dni) {

        if (dni <= 0) {
            throw new IllegalArgumentException("El DNI del cliente debe ser mayor a 0");
        }

        Cliente cliente = clienteDao.find(dni, true);
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no existe");
        }
        return cliente;
    }

    /**
     * Elimina un cliente de la base en memoria, si existe, utilizando su DNI.
     *
     * @param dni DNI del cliente que se quiere borrar.
     * @throws IllegalArgumentException si el cliente no se encuentra en la base en memoria.
     */
    public void borrarCliente(long dni) {

        if (dni <= 0) {
            throw new IllegalArgumentException("El DNI del cliente debe ser mayor a 0");
        }

        if (clienteDao.find(dni, false) == null) {
            throw new IllegalArgumentException("El cliente no existe");
        } else {
            clienteDao.delete(dni);
        }
    }
}