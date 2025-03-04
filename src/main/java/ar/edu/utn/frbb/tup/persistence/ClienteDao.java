package ar.edu.utn.frbb.tup.persistence;

import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.persistence.entity.ClienteEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DAO (Data Access Object) dedicado a las operaciones de persistencia de la entidad Cliente.
 * Utiliza una base de datos en memoria (inMemoryDatabase) para guardar y recuperar clientes.
 */
@Service
public class ClienteDao extends AbstractBaseDao {

    // Referencia al DAO de Cuenta, para poder cargar las cuentas asociadas a un cliente cuando sea necesario.
    @Autowired
    private CuentaDao cuentaDao;

    /**
     * Busca un cliente por su DNI dentro de la base en memoria.
     * Si loadComplete es true, se cargan también las cuentas asociadas.
     *
     * @param dni         DNI del cliente buscado.
     * @param loadComplete Indica si se deben cargar las cuentas del cliente.
     * @return Cliente reconstruido o null si no se encuentra.
     */
    public Cliente find(long dni, boolean loadComplete) {
        // Primero verifica si el cliente existe en la BD en memoria.
        if (getInMemoryDatabase().get(dni) == null)
            return null;

        // Reconstruye el objeto Cliente a partir de un ClienteEntity.
        Cliente cliente = ((ClienteEntity) getInMemoryDatabase().get(dni)).toCliente();

        // Si se pide cargar las cuentas, se obtienen desde cuentaDao y se agregan al cliente.
        if (loadComplete) {
            for (Cuenta cuenta : cuentaDao.getCuentasByCliente(dni)) {
                cliente.addCuenta(cuenta);
            }
        }
        return cliente;
    }

    /**
     * Guarda un objeto Cliente en la base en memoria, creando un ClienteEntity para ello.
     *
     * @param cliente Objeto de negocio que deseamos persistir.
     */
    public void save(Cliente cliente) {
        ClienteEntity entity = new ClienteEntity(cliente);
        getInMemoryDatabase().put(entity.getId(), entity);
    }

    /**
     * Elimina de la base en memoria el cliente identificado por el DNI especificado.
     *
     * @param dni DNI del cliente que se desea eliminar.
     */
    public void delete(long dni) {
        getInMemoryDatabase().remove(dni);
    }

    /**
     * Retorna el nombre con el que se identifican los clientes en la BD en memoria.
     *
     * @return String que representa el nombre de la entidad, en este caso "CLIENTE".
     */
    @Override
    protected String getEntityName() {
        return "CLIENTE";
    }
}