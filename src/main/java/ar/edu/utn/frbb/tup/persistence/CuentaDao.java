package ar.edu.utn.frbb.tup.persistence;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.persistence.entity.CuentaEntity;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) que maneja las operaciones de persistencia para la entidad Cuenta.
 * Se apoya en una base de datos en memoria (un mapa) para almacenar los registros.
 */
@Component
public class CuentaDao extends AbstractBaseDao {

    /**
     * Retorna el nombre de la entidad que estamos manejando en la BD en memoria.
     * En este caso, la entidad se identifica como "CUENTA".
     *
     * @return String "CUENTA".
     */
    @Override
    protected String getEntityName() {
        return "CUENTA";
    }

    /**
     * Guarda la información de una Cuenta en la base en memoria,
     * transformándola primero en un CuentaEntity para su persistencia.
     *
     * @param cuenta Objeto Cuenta de la capa de negocio que se desea persistir.
     */
    public void save(Cuenta cuenta) {
        CuentaEntity entity = new CuentaEntity(cuenta);
        getInMemoryDatabase().put(entity.getId(), entity);
    }

    /**
     * Busca una cuenta en el mapa de la BD en memoria, utilizando su ID (número de cuenta).
     * Retorna null si no existe. De lo contrario, convierte el CuentaEntity recuperado en un objeto Cuenta.
     *
     * @param id Identificador de la cuenta (número de cuenta).
     * @return Objeto Cuenta resultante, o null si no se encontró.
     */
    public Cuenta find(long id) {
        if (getInMemoryDatabase().get(id) == null) {
            return null;
        }
        CuentaEntity e = (CuentaEntity) getInMemoryDatabase().get(id);
        return e.toCuenta();
    }

    /**
     * Obtiene todas las cuentas asociadas a un cliente, basándose en su DNI.
     * Recorre todos los registros de la BD en memoria y filtra aquellos que coinciden con el DNI del titular.
     *
     * @param dni DNI del titular cuyas cuentas se requieren.
     * @return Lista de objetos Cuenta correspondientes a ese titular.
     */
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

    /**
     * Incrementa el balance de una cuenta específica, identificada por su ID.
     * Si la cuenta no existe, lanza IllegalArgumentException.
     *
     * @param id    Identificador de la cuenta que se quiere actualizar.
     * @param monto Cantidad a sumar al balance actual.
     */
    public void agregarBalance(long id, int monto) {
        // Se obtiene la CuentaEntity desde el mapa en memoria.
        CuentaEntity cuentaEntity = (CuentaEntity) getInMemoryDatabase().get(id);

        if (cuentaEntity != null) {
            // Se calcula el nuevo balance y se actualiza en la entidad.
            int nuevoBalance = cuentaEntity.getBalance() + monto;
            cuentaEntity.setBalance(nuevoBalance);

            // Se registra el cambio en la base de datos en memoria.
            getInMemoryDatabase().put(id, cuentaEntity);
        } else {
            throw new IllegalArgumentException("No se encontró la cuenta con ID: " + id);
        }
    }
}