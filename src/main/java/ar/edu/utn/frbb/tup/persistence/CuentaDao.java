package ar.edu.utn.frbb.tup.persistence;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.persistence.entity.CuentaEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CuentaDao extends AbstractBaseDao {

    private static final String FILENAME = "cuentas.txt";

    public void save(Cuenta cuenta) {
        CuentaEntity entity = new CuentaEntity(cuenta);
        getInMemoryDatabase().put(entity.getId(), entity);
        saveToFile(FILENAME, entity.toString());
    }

    public Cuenta find(long cvu) {
        if (getInMemoryDatabase().get(cvu) == null) {
            return null;
        }
        return ((CuentaEntity) getInMemoryDatabase().get(cvu)).toCuenta();
    }

    public List<Cuenta> getCuentasByCliente(long dni) {
        List<Cuenta> cuentasDelCliente = new ArrayList<>();
        for (Object object : getInMemoryDatabase().values()) {
            CuentaEntity cuenta = ((CuentaEntity) object);
            if (cuenta.getTitular().equals(dni)) {
                cuentasDelCliente.add(cuenta.toCuenta());
            }
        }
        return cuentasDelCliente;
    }

    public List<String> findAll() {
        return readFromFile(FILENAME);
    }

    @Override
    protected String getEntityName() {
        return "CUENTA";
    }
}
