package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;

public class TransferService {

    public TransferEntity makeTransfer(Long fromAccountId, Long toAccountId, Double amount, String description) {
        // Implement the logic to handle the transfer
        // Validate accounts, check balances, update balances, etc.
        
        TransferEntity transfer = new TransferEntity();
        transfer.setFromAccountId(fromAccountId);
        transfer.setToAccountId(toAccountId);
        transfer.setAmount(amount);
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setDescription(description);

        // Save the transfer to the database (pseudo-code)
        // transferRepository.save(transfer);

        return transfer;
    }
}