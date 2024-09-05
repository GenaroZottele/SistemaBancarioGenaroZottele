package ar.edu.utn.frbb.tup.persistence;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.persistence.entity.ClienteEntity;
import ar.edu.utn.frbb.tup.persistence.entity.CuentaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CuentaDao  extends AbstractBaseDao{
    @Override
    protected String getEntityName() {
        return "CUENTA";
    }

    public void save(Cuenta cuenta) {
        CuentaEntity entity = new CuentaEntity(cuenta);
        getInMemoryDatabase().put(entity.getId(), entity);
    }

    public Cuenta find(long id) {
        if (getInMemoryDatabase().get(id) == null) {
            return null;
        }
        return ((CuentaEntity) getInMemoryDatabase().get(id)).toCuenta();
    }

    public List<Cuenta> getCuentasByCliente(long dni) {
        List<Cuenta> cuentasDelCliente = new ArrayList<>();
        for (Object object:
                getInMemoryDatabase().values()) {
            CuentaEntity cuenta = ((CuentaEntity) object);
            if (cuenta.getTitular().equals(dni)) {
                cuentasDelCliente.add(cuenta.toCuenta());
            }
        }
        return cuentasDelCliente;
    }
    // Método para agregar balance a una cuenta
    public void agregarBalance(long id, int monto) {
        // Obtener la entidad de cuenta por id
        CuentaEntity cuentaEntity = (CuentaEntity) getInMemoryDatabase().get(id);

        if (cuentaEntity != null) {
            // Incrementar el balance de la cuenta
            int nuevoBalance = cuentaEntity.getBalance() + monto;
            cuentaEntity.setBalance(nuevoBalance);

            // Guardar los cambios en la base de datos en memoria
            getInMemoryDatabase().put(id, cuentaEntity);
        } else {
            throw new IllegalArgumentException("No se encontró la cuenta con ID: " + id);
        }
    }
}