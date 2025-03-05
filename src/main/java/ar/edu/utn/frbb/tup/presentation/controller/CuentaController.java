package ar.edu.utn.frbb.tup.presentation.controller;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaNotSupportedException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cuenta")
public class CuentaController {

    @Autowired
    private CuentaService cuentaService;

    @PostMapping("/{dni}")
    public ResponseEntity<?> crearCuenta(@RequestBody Cuenta cuenta, @PathVariable long dni) {
        try {
            Cuenta nuevaCuenta = cuentaService.darDeAltaCuenta(cuenta, dni);
            return ResponseEntity.ok(nuevaCuenta);
        } catch (TipoCuentaAlreadyExistsException | CuentaAlreadyExistsException | TipoCuentaNotSupportedException e) {
            // Si ocurre alguna de estas excepciones, se devuelve 400 Bad Request con el mensaje del error
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Para cualquier otro error se devuelve 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la cuenta: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/agregar-balance")
    public ResponseEntity<String> agregarBalance(@PathVariable long id, @RequestParam int monto) {
        // En este endpoint podemos seguir usando try/catch si se desea enviar mensajes personalizados
        try {
            cuentaService.agregarBalance(id, monto);
            return ResponseEntity.ok("Balance agregado exitosamente a la cuenta con ID: " + id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al agregar balance: " + e.getMessage());
        }
    }

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

    @GetMapping("/cliente/{dni}")
    public ResponseEntity<?> getCuentasByCliente(@PathVariable long dni) {
        try {
            List<Cuenta> cuentas = cuentaService.getCuentasByCliente(dni);
            return ResponseEntity.ok(cuentas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener cuentas del cliente: " + e.getMessage());
        }
    }
}
