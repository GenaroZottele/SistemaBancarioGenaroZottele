package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import ar.edu.utn.frbb.tup.presentation.dto.CuentaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CuentaService {
    CuentaDao cuentaDao = new CuentaDao();

    @Autowired
    ClienteService clienteService;

    //Generar casos de test para darDeAltaCuenta
    //    1 - cuenta existente
    //    2 - cuenta no soportada
    //    3 - cliente ya tiene cuenta de ese tipo
    //    4 - cuenta creada exitosamente
    public Cuenta darDeAltaCuenta(Cuenta cuenta, long dniTitular) throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException {
        if(cuentaDao.find(cuenta.getNumeroCuenta()) != null) {
            throw new CuentaAlreadyExistsException("La cuenta " + cuenta.getNumeroCuenta() + " ya existe.");
        }
        //Chequear cuentas soportadas por el banco CA$ CC$ CAU$S
        // if (!tipoCuentaEstaSoportada(cuenta)) {...}

        clienteService.agregarCuenta(cuenta, dniTitular);
        cuentaDao.save(cuenta);
        return cuenta;
    }
    // Método para agregar balance a una cuenta
    public void agregarBalance(long id, int monto) {
        // Validar que el monto no sea negativo
        if (monto < 0) {
            throw new IllegalArgumentException("El monto a agregar no puede ser negativo.");
        }

        // Llamar al método de CuentaDao para agregar el balance
        cuentaDao.agregarBalance(id, monto);
    }
    // Método para buscar una cuenta por ID
    public Cuenta findCuentaById(long id) {
        Cuenta cuenta = cuentaDao.find(id);

        if (cuenta == null) {
            throw new IllegalArgumentException("No se encontró la cuenta con ID: " + id);
        }

        return cuenta;
    }
}
