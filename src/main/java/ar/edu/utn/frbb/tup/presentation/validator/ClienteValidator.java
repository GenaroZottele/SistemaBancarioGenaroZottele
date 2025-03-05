package ar.edu.utn.frbb.tup.presentation.validator;

import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;
import org.springframework.stereotype.Component;

@Component
public class ClienteValidator {

    public void validate(ClienteDto clienteDto) {
        if (clienteDto == null) {
            throw new IllegalArgumentException("El ClienteDto no puede ser nulo");
        }
        if (clienteDto.getDni() <= 0) {
            throw new IllegalArgumentException("El DNI del cliente debe ser mayor a 0");
        }
        if (clienteDto.getNombre() == null || clienteDto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        if (clienteDto.getApellido() == null || clienteDto.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del cliente no puede estar vacío");
        }
        if (clienteDto.getFechaNacimiento() == null || clienteDto.getFechaNacimiento().trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha de nacimiento del cliente no puede estar vacía");
        }
        if (clienteDto.getTipoPersona() == null || !isValidTipoPersona(clienteDto.getTipoPersona())) {
            throw new IllegalArgumentException("El tipo de persona no es correcto");
        }
    }

    private boolean isValidTipoPersona(String tipoPersona) {
        // Asumimos que para PERSONA_FISICA se espera "F" y para PERSONA_JURIDICA "J"
        return "F".equalsIgnoreCase(tipoPersona) || "J".equalsIgnoreCase(tipoPersona);
    }
}
