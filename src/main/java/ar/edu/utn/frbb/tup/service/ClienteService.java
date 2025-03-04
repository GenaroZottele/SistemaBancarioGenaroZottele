package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;
import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    ClienteDao clienteDao;

    public ClienteService(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    public Cliente darDeAltaCliente(ClienteDto clienteDto) throws ClienteAlreadyExistsException {
        
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
        
        Cliente cliente = new Cliente(clienteDto);

        if (clienteDao.find(cliente.getDni(), false) != null) {
            throw new ClienteAlreadyExistsException("Ya existe un cliente con DNI " + cliente.getDni());
        }

        if (cliente.getEdad() < 18) {
            throw new IllegalArgumentException("El cliente debe ser mayor a 18 años");
        }

        clienteDao.save(cliente);
        return cliente;
    }

    public void agregarCuenta(Cuenta cuenta, long dniTitular) throws TipoCuentaAlreadyExistsException {
        
        if (cuenta == null) {
            throw new IllegalArgumentException("La cuenta no puede ser nula");
        }
        if (dniTitular <= 0) {
            throw new IllegalArgumentException("El DNI del titular debe ser mayor a 0");
        }
        
        Cliente titular = buscarClientePorDni(dniTitular);
        cuenta.setTitular(titular);
        if (titular.tieneCuenta(cuenta.getTipoCuenta(), cuenta.getMoneda())) {
            throw new TipoCuentaAlreadyExistsException("El cliente ya posee una cuenta de ese tipo y moneda");
        }
        titular.addCuenta(cuenta);
        clienteDao.save(titular);
    }

    public Cliente buscarClientePorDni(long dni) {

        if (dni <= 0) {
            throw new IllegalArgumentException("El DNI del cliente debe ser mayor a 0");
        }

        Cliente cliente = clienteDao.find(dni, true);
        if(cliente == null) {
            throw new IllegalArgumentException("El cliente no existe");
        }
        return cliente;
    }

    public void borrarCliente(long dni) {

        if (dni <= 0) {
            throw new IllegalArgumentException("El DNI del cliente debe ser mayor a 0");
        }
        
        if (clienteDao.find(dni, false) == null) {
            throw new IllegalArgumentException("El cliente no existe");
        }else {
            clienteDao.delete(dni);
        }
    }
}