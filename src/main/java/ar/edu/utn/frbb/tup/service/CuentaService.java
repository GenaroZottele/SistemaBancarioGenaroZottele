package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaNotSupportedException;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CuentaService {
    CuentaDao cuentaDao = new CuentaDao();

    @Autowired
    ClienteService clienteService;


    public boolean tipoDeCuentaSoportada(Cuenta cuenta) {
        if (cuenta.getTipoCuenta().equals(TipoCuenta.NO_SOPORTADA) ) {
            return false;
        }
        return true;
    }

    public void darDeAltaCuenta(Cuenta cuenta, long dniTitular) throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoCuentaNotSupportedException {
        if (cuentaDao.find(cuenta.getCVU()) != null) {
            throw new CuentaAlreadyExistsException("La cuenta " + cuenta.getCVU() + " ya existe.");
        }

        if (!tipoDeCuentaSoportada(cuenta)) {
            throw new TipoCuentaNotSupportedException("Tipo de cuenta no soportada");
        }

        clienteService.agregarCuenta(cuenta, dniTitular);
        cuentaDao.save(cuenta);
    }

    public Cuenta find(long id) {
        return cuentaDao.find(id);
    }
}
