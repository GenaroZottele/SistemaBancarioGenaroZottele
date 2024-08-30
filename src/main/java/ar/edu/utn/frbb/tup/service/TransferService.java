package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransferService {

    public TransferEntity makeTransfer(Long origen, Long destino, Double monto, String descripcion) {
        // Implement the logic to handle the transfer
        // Validate accounts, check balances, update balances, etc.
        
        TransferEntity transfer = new TransferEntity();
        transfer.setOrigen(origen);
        transfer.setDestino(destino);
        transfer.setMonto(monto);
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setDescripcion(descripcion);

        // Save the transfer to the database (pseudo-code)
        // transferRepository.save(transfer);

        return transfer;
    }
}