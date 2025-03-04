package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import ar.edu.utn.frbb.tup.presentation.dto.CuentaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CuentaService {

    @Autowired
    private CuentaDao cuentaDao;

    @Autowired
    private ClienteService clienteService;

    /**
     * Dar de alta una cuenta para un cliente existente.
     *
     * @param cuenta Objeto Cuenta con datos básicos (numeroCuenta, balance, tipoCuenta, moneda).
     * @param dniTitular DNI del cliente titular.
     */
    public Cuenta darDeAltaCuenta(Cuenta cuenta, long dniTitular)
            throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException {
        
        if (cuenta == null) {
            throw new IllegalArgumentException("La cuenta no puede ser nula");
        }
        if (dniTitular <= 0) {
            throw new IllegalArgumentException("El DNI del titular debe ser mayor a 0");
        }
        if (cuenta.getMoneda() == null) {
            throw new IllegalArgumentException("La moneda de la cuenta no puede ser nula");
        }
        if (cuenta.getTipoCuenta() == null) {
            throw new IllegalArgumentException("El tipo de cuenta no puede ser nulo");
        }
        if (cuenta.getBalance() < 0) {
            throw new IllegalArgumentException("El balance inicial de la cuenta no puede ser negativo");
        }        

        // 1) Verificar si la cuenta ya existe
        if (cuentaDao.find(cuenta.getNumeroCuenta()) != null) {
            throw new CuentaAlreadyExistsException("La cuenta " + cuenta.getNumeroCuenta() + " ya existe.");
        }

        // 2) Verificar si el tipo de cuenta es soportado
        if (!esTipoDeCuentaSoportado(cuenta.getTipoCuenta().toString())) {
            // En tus tests esperás otra excepción o la usás así. Ajustá si tenés una custom exception.
            throw new IllegalArgumentException("El tipo de cuenta no es soportado.");
        }

        // 3) Buscar cliente real (en vez de new Cliente())
        Cliente cliente = clienteService.buscarClientePorDni(dniTitular);
        if (cliente == null) {
            throw new IllegalArgumentException("No existe un cliente con DNI " + dniTitular);
        }

        // 4) Verificar si el cliente ya tiene una cuenta con el MISMO tipo Y la MISMA moneda
        boolean existeMismoTipoYMoneda = cuentaDao.getCuentasByCliente(cliente.getDni())
            .stream()
            .anyMatch(c -> c.getTipoCuenta().equals(cuenta.getTipoCuenta())
                && c.getMoneda().equals(cuenta.getMoneda()));

        if (existeMismoTipoYMoneda) {
            throw new IllegalArgumentException("El cliente ya tiene una cuenta de este tipo y moneda.");
        }

        // 5) Vincular la cuenta con el titular usando el método de ClienteService 
        // (este método setea cuenta.setTitular(cliente) y hace las validaciones de "cuenta repetida")
        clienteService.agregarCuenta(cuenta, dniTitular);

        // 6) Guardar la cuenta
        cuentaDao.save(cuenta);
        return cuenta;
    }

    /**
     * Agrega saldo a la cuenta dada.
     */
    public void agregarBalance(long id, int monto) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la cuenta debe ser mayor a 0");
        }
        if (monto < 0) {
            throw new IllegalArgumentException("El monto a agregar no puede ser negativo.");
        }
        cuentaDao.agregarBalance(id, monto);
    }

    /**
     * Busca una cuenta por ID, y "hidrata" su titular a partir del dniTitular que guardamos en la entidad.
     */
    public Cuenta findCuentaById(long id) {
        
        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la cuenta debe ser mayor a 0");
        }
        
        // 1) Buscar la cuenta en la DAO
        Cuenta cuenta = cuentaDao.find(id);

        if (cuenta == null) {
            throw new IllegalArgumentException("No se encontró la cuenta con ID: " + id);
        }

        // 2) Si la cuenta tiene un DNI de titular, cargamos el cliente desde ClienteService
        if (cuenta.getDniTitular() != null) {
            // Cargar el cliente
            Cliente titular = clienteService.buscarClientePorDni(cuenta.getDniTitular());
            cuenta.setTitular(titular);
        }

        return cuenta;
    }

    /**
     * Retorna todas las cuentas para el cliente con DNI especificado.
     */
    public List<Cuenta> getCuentasByCliente(long dni) {
        
        if (dni <= 0) {
            throw new IllegalArgumentException("El DNI del cliente debe ser mayor a 0");
        }
        
        List<Cuenta> cuentas = cuentaDao.getCuentasByCliente(dni);
        if (cuentas.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron cuentas para el cliente con DNI: " + dni);
        }

        // Opcional: si querés hidratar a cada una con su titular, podés hacerlo acá:
        // for (Cuenta c : cuentas) {
        //     if (c.getDniTitular() != null) {
        //         Cliente titular = clienteService.buscarClientePorDni(c.getDniTitular());
        //         c.setTitular(titular);
        //     }
        // }
        return cuentas;
    }

    private boolean esTipoDeCuentaSoportado(String tipoCuenta) {
        // Ajustar según lo que tu banco soporte
        return tipoCuenta.equals("CUENTA_CORRIENTE") || tipoCuenta.equals("CAJA_AHORRO");
    }
}
