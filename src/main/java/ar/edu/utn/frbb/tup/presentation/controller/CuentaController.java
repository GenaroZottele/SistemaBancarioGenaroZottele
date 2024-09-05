package ar.edu.utn.frbb.tup.presentation.controller;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.presentation.dto.CuentaDto;
import ar.edu.utn.frbb.tup.service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cuenta")
public class CuentaController {

    @Autowired
    private CuentaService cuentaService;

    @PostMapping("/{dni}")
    public Cuenta crearCuenta(@RequestBody Cuenta cuenta, @PathVariable long dni) throws TipoCuentaAlreadyExistsException, CuentaAlreadyExistsException {
        cuentaService.darDeAltaCuenta(cuenta, dni);
        return cuenta;
    }
    // Endpoint para agregar balance a una cuenta
    @PostMapping("/{id}/agregar-balance")
    public ResponseEntity<String> agregarBalance(@PathVariable long id, @RequestParam int monto) {
        try {
            cuentaService.agregarBalance(id, monto);
            return ResponseEntity.ok("Balance agregado exitosamente a la cuenta con ID: " + id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al agregar balance: " + e.getMessage());
        }
    }
    // Endpoint para buscar una cuenta por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> findCuentaById(@PathVariable long id) {
        try {
            Cuenta cuenta = cuentaService.findCuentaById(id);
            return ResponseEntity.ok(cuenta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al buscar la cuenta: " + e.getMessage());
        }
    }
}
