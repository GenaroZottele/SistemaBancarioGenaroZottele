package ar.edu.utn.frbb.tup.presentation.controller;

import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;
import ar.edu.utn.frbb.tup.presentation.dto.TransferDto;
import ar.edu.utn.frbb.tup.presentation.dto.TransferResponseDto;
import ar.edu.utn.frbb.tup.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponseDto> makeTransfer(@RequestBody TransferDto transferDto) {
        TransferResponseDto response = new TransferResponseDto();
        try {
            transferService.makeTransfer(
                    transferDto.getOrigen(),
                    transferDto.getDestino(),
                    transferDto.getMonto(),
                    transferDto.getDescripcion(),
                    transferDto.getMoneda()
            );
            response.setEstado("EXITOSA");
            response.setMensaje("Transferencia exitosa");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setEstado("FALLIDA");
            response.setMensaje("Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/movements/{id}")
    public List<TransferEntity> getMovements(@PathVariable Long id) {
        return transferService.getMovements(id);
    }
}
