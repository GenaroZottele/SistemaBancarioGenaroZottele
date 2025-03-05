package ar.edu.utn.frbb.tup.validator;

import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;
import ar.edu.utn.frbb.tup.presentation.validator.ClienteValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteValidatorTest {

    private ClienteValidator validator = new ClienteValidator();

    @Test
    void testValidate_ClienteDtoNull() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(null),
                "El ClienteDto no puede ser nulo");
    }

    @Test
    void testValidate_DniInvalido() {
        ClienteDto dto = new ClienteDto();
        dto.setDni(0); // DNI <= 0

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(dto));
        assertTrue(e.getMessage().contains("El DNI del cliente debe ser mayor a 0"));
    }

    @Test
    void testValidate_NombreVacio() {
        ClienteDto dto = new ClienteDto();
        dto.setDni(123);
        dto.setNombre("   "); // Nombre vacío
        dto.setApellido("Pérez");
        dto.setFechaNacimiento("1990-01-01");
        dto.setTipoPersona("F");

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(dto));
        assertTrue(e.getMessage().contains("El nombre del cliente no puede estar vacío"));
    }

    @Test
    void testValidate_TipoPersonaInvalido() {
        ClienteDto dto = new ClienteDto();
        dto.setDni(123);
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setFechaNacimiento("1990-01-01");
        // 'Z' no es un tipoPersona válido
        dto.setTipoPersona("Z");

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(dto));
        assertTrue(e.getMessage().contains("El tipo de persona no es correcto"));
    }

    @Test
    void testValidate_Exito() {
        ClienteDto dto = new ClienteDto();
        dto.setDni(123);
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setFechaNacimiento("1990-01-01");
        dto.setTipoPersona("F");

        assertDoesNotThrow(() -> validator.validate(dto));
    }
    @Test
    void testValidate_ApellidoVacio() {
        ClienteDto dto = new ClienteDto();
        dto.setDni(123);
        dto.setNombre("Juan");
        dto.setApellido("   "); // apellido vacío
        dto.setFechaNacimiento("1990-01-01");
        dto.setTipoPersona("F");

        Exception e = assertThrows(IllegalArgumentException.class, () -> validator.validate(dto));
        assertTrue(e.getMessage().contains("El apellido del cliente no puede estar vacío"));
    }

}
