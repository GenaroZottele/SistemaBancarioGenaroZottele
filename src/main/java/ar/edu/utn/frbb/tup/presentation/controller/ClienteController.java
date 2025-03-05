package ar.edu.utn.frbb.tup.presentation.controller;

import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;
import ar.edu.utn.frbb.tup.presentation.validator.ClienteValidator;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteValidator clienteValidator;

    // Endpoint para crear un cliente.
    @PostMapping
    public ResponseEntity<?> crearCliente(@RequestBody ClienteDto clienteDto) {
        try {
            if (clienteDto == null) {
                throw new IllegalArgumentException("El ClienteDto no puede ser nulo");
            }
            if (clienteDto.getNombre() == null || clienteDto.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
            }
            // Forzamos que, en caso de datos inválidos, el validator lance excepción.
            clienteValidator.validate(clienteDto);
            Cliente cliente = clienteService.darDeAltaCliente(clienteDto);
            return ResponseEntity.ok(cliente);
        } catch (ClienteAlreadyExistsException ex) {
            // Se devuelve 409 para errores de cliente ya existente.
            return ResponseEntity.status(409).body(ex.getMessage());
        } catch (Exception ex) {
            // Se devuelve 400 para errores de validación o de negocio.
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{dni}")
    public ResponseEntity<?> buscarCliente(@PathVariable long dni) {
        try {
            Cliente cliente = clienteService.buscarClientePorDni(dni);
            return ResponseEntity.ok(cliente);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Endpoint para borrar un cliente.
    @DeleteMapping("/{dni}")
    public ResponseEntity<?> borrarCliente(@PathVariable long dni) {
        try {
            clienteService.borrarCliente(dni);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            // Devolvemos 400 si ocurre un error (por ejemplo, cliente no existe)
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
