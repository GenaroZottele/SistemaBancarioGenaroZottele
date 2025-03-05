package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.model.exception.CantidadNegativaException;
import ar.edu.utn.frbb.tup.model.exception.NoAlcanzaException;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import ar.edu.utn.frbb.tup.persistence.TransferDao;
import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferService {

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private TransferDao transferDao;

    /**
     * Realiza la transferencia, validando reglas de negocio:
     * - Cuentas existentes
     * - Misma moneda
     * - Fondos suficientes
     * - Cálculo de comisión si corresponde
     * - Actualización de saldo
     * - Guardado de la TransferEntity
     *
     * @param origen    ID de cuenta origen
     * @param destino   ID de cuenta destino
     * @param monto     Monto a transferir
     * @param descripcion Descripción de la transferencia
     * @param moneda    PESOS o DOLARES
     * @return TransferEntity con los datos de la transferencia
     */
    public TransferEntity makeTransfer(Long origen, Long destino, Double monto, String descripcion, TipoMoneda moneda) {
        
        if (origen == null || origen <= 0) {
            throw new IllegalArgumentException("La cuenta de origen debe ser un ID válido (>0)");
        }
        if (destino == null || destino <= 0) {
            throw new IllegalArgumentException("La cuenta de destino debe ser un ID válido (>0)");
        }
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("No se permite transferir entre la misma cuenta (mismo origen y destino)");
        }
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a transferir debe ser mayor a 0");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        if (moneda == null) {
            throw new IllegalArgumentException("La moneda no puede ser nula");
        }
        
        //Buscar cuentas
        Cuenta cuentaOrigen = cuentaService.findCuentaById(origen);
        Cuenta cuentaDestino = cuentaService.findCuentaById(destino);

        //Validaciones
        if (cuentaOrigen == null) {
            throw new IllegalArgumentException("La cuenta de origen no existe: " + origen);
        }
        if (cuentaDestino == null) {
            throw new IllegalArgumentException("La cuenta destino no existe en nuestro banco: " + destino);
        }
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("La cuenta de origen y destino no pueden ser la misma");
        }
        // Verificar moneda
        if (cuentaOrigen.getMoneda() != cuentaDestino.getMoneda()) {
            throw new IllegalArgumentException("Las cuentas deben tener la misma moneda para transferir");
        }
        // Asegurar que la moneda que viene en la petición coincide con la moneda real de la cuenta
        if (cuentaOrigen.getMoneda() != moneda) {
            throw new IllegalArgumentException("La moneda del request no coincide con la moneda de la cuenta origen");
        }

        //Calcular la comisión si corresponde
        double comision = 0.0;
        if (moneda == TipoMoneda.PESOS && monto > 1000000) {
            comision = monto * 0.02; // 2%
        } else if (moneda == TipoMoneda.DOLARES && monto > 5000) {
            comision = monto * 0.005; // 0.5%
        }
        double totalADebitar = monto + comision;

        //Verificar saldo suficiente en cuentaOrigen
        if (cuentaOrigen.getBalance() < totalADebitar) {
            throw new IllegalArgumentException("Fondos insuficientes en la cuenta origen");
        }

        //Actualizar saldos
        int debit = (int)Math.round(totalADebitar);
        int credit = (int)Math.round(monto);

        try {
            cuentaOrigen.debitarDeCuenta(debit);
        } catch (NoAlcanzaException | CantidadNegativaException e) {
            throw new IllegalArgumentException("Error al debitar la cuenta origen: " + e.getMessage());
        }

        // Acreditamos en cuenta destino
        int nuevoBalanceDestino = cuentaDestino.getBalance() + credit;
        cuentaDestino.setBalance(nuevoBalanceDestino);

        //Persistimos los cambios en el "Dao" (para que no se pierdan)
        CuentaDao cuentaDao = new CuentaDao();
        cuentaDao.save(cuentaOrigen);
        cuentaDao.save(cuentaDestino);

        //Construir la TransferEntity y guardarla
        TransferEntity transfer = new TransferEntity();
        transfer.setId(System.currentTimeMillis()); // o un generador de ID
        transfer.setOrigen(origen);
        transfer.setDestino(destino);
        transfer.setMonto(monto);
        transfer.setDescripcion(descripcion);
        transfer.setMoneda(moneda);
        transfer.setTransferDate(LocalDateTime.now());

        transferDao.save(transfer);

        //Retornar la TransferEntity resultante
        return transfer;
    }

    public List<TransferEntity> getMovements(Long cvu) {
        
        if (cvu == null || cvu <= 0) {
            throw new IllegalArgumentException("El CVU debe ser un valor válido (>0)");
        }
        
        return transferDao.findAllByCuenta(cvu);
    }
}
