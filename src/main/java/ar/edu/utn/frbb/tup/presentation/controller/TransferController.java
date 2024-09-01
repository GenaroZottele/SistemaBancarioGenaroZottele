package ar.edu.utn.frbb.tup.presentation.controller;

import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;
import ar.edu.utn.frbb.tup.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @PostMapping("/transfer")
    public TransferEntity makeTransfer(@RequestParam Long fromAccountId, @RequestParam Long toAccountId, @RequestParam Double amount, @RequestParam String description) {
        return transferService.makeTransfer(fromAccountId, toAccountId, amount, description);
    }
    @GetMapping("/movements/{CVU}")
    public List<TransferEntity> getMovements(@PathVariable Long CVU) {
        return transferService.getMovements(CVU); //VER ENTITY
    }
}