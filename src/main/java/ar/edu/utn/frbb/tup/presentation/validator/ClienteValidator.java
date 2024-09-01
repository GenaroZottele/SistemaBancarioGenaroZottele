package ar.edu.utn.frbb.tup.presentation.validator;

import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;
import org.springframework.stereotype.Component;

@Component
public class ClienteValidator {

    public void validate(ClienteDto clienteDto) {
        if (clienteDto.getTipoPersona() == null || !isValidTipoPersona(clienteDto.getTipoPersona())) {
            throw new IllegalArgumentException("El tipo de persona no es correcto");
        }


        // Add other validations as needed
    }

    private boolean isValidTipoPersona(String tipoPersona) {
        return "F".equalsIgnoreCase(tipoPersona) || "J".equalsIgnoreCase(tipoPersona);
    }
}
