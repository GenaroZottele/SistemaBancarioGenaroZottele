package ar.edu.utn.frbb.tup.persistence;

import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransferDao extends AbstractBaseDao {

    private static final String FILENAME = "transfers.txt";

    /**
     * Guarda la entidad en memoria y en el archivo, serializándola como CSV
     */
    public void save(TransferEntity transfer) {
        // 1) Guardar en el inMemoryDatabase
        getInMemoryDatabase().put(transfer.getId(), transfer);

        // 2) Serializarla a String (CSV) y guardarla en el archivo
        String csv = objectToString(transfer);
        saveToFile(FILENAME, csv);
    }

    /**
     * Busca una transferencia en la memoria por ID (no lee del archivo)
     */
    public TransferEntity find(long id) {
        if (getInMemoryDatabase().get(id) == null) {
            return null;
        }
        return (TransferEntity) getInMemoryDatabase().get(id);
    }

    /**
     * Lee todas las líneas del archivo (CSV en crudo)
     */
    public List<String> findAll() {
        return readFromFile(FILENAME);
    }

    /**
     * Retorna todas las transferencias parseadas como objetos.
     * Lee línea a línea el archivo y las convierte en TransferEntity.
     */
    public List<TransferEntity> findAllTransfers() {
        List<String> lines = findAll();
        List<TransferEntity> transfers = new ArrayList<>();
        for (String line : lines) {
            TransferEntity t = (TransferEntity) stringToObject(line);
            if (t != null) {
                transfers.add(t);
            }
        }
        return transfers;
    }

    /**
     * Busca todas las transferencias donde la cuenta origen o destino coincidan con 'cuentaId'
     */
    public List<TransferEntity> findAllByCuenta(Long cuentaId) {
        List<TransferEntity> transfers = new ArrayList<>();
        // 1) Podés leer todas las transferencias parseadas del archivo
        List<TransferEntity> all = findAllTransfers();

        // 2) Filtrarlas
        for (TransferEntity t : all) {
            if (t.getOrigen().equals(cuentaId) || t.getDestino().equals(cuentaId)) {
                transfers.add(t);
            }
        }
        return transfers;
    }

    @Override
    protected String getEntityName() {
        return "TRANSFER";
    }

    /**
     * Convierte un TransferEntity en un string CSV, por ejemplo:
     * id;origen;destino;monto;descripcion;moneda;fecha
     */
    protected String objectToString(Object object) {
        TransferEntity t = (TransferEntity) object;
        String safeDescripcion = t.getDescripcion() == null ? "" : t.getDescripcion().replace(";", ",");
        return t.getId() + ";" +
               t.getOrigen() + ";" +
               t.getDestino() + ";" +
               t.getMonto() + ";" +
               safeDescripcion + ";" +
               t.getMoneda() + ";" +
               t.getTransferDate();
    }

    /**
     * Convierte un string CSV en un TransferEntity
     */
    
    protected Object stringToObject(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(";");
        // Esperamos 7 partes => (id, origen, destino, monto, desc, moneda, fecha)
        if (parts.length < 7) {
            return null;  // o lanzar excepción
        }

        try {
            TransferEntity t = new TransferEntity();
            t.setId(Long.parseLong(parts[0].trim()));
            t.setOrigen(Long.parseLong(parts[1].trim()));
            t.setDestino(Long.parseLong(parts[2].trim()));
            t.setMonto(Double.parseDouble(parts[3].trim()));
            t.setDescripcion(parts[4]); // la desc ya no tiene ';'
            t.setMoneda(TipoMoneda.valueOf(parts[5].trim()));
            t.setTransferDate(LocalDateTime.parse(parts[6].trim()));
            return t;
        } catch (Exception e) {
            return null;
        }
    }
}
