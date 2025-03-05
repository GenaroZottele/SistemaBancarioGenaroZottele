package ar.edu.utn.frbb.tup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;
import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.presentation.validator.ClienteValidator;
import ar.edu.utn.frbb.tup.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ar.edu.utn.frbb.tup.presentation.controller.ClienteController;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;
import ar.edu.utn.frbb.tup.presentation.handler.TupResponseEntityExceptionHandler;

@Import(TupResponseEntityExceptionHandler.class)

@WebMvcTest(ClienteController.class)
public class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private ClienteValidator clienteValidator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCrearCliente_Exito() throws Exception, ClienteAlreadyExistsException {
        ClienteDto clienteDto = new ClienteDto();
        clienteDto.setDni(12345678);
        clienteDto.setNombre("Juan");
        clienteDto.setApellido("Pérez");
        clienteDto.setFechaNacimiento("1990-01-01");
        clienteDto.setTipoPersona("PERSONA_FISICA");

        Cliente cliente = new Cliente(clienteDto);
        // Simular validación exitosa y creación del cliente
        doNothing().when(clienteValidator).validate(any(ClienteDto.class));
        when(clienteService.darDeAltaCliente(any(ClienteDto.class))).thenReturn(cliente);

        mockMvc.perform(post("/cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(12345678))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.apellido").value("Pérez"));
    }

    @Test
    public void testCrearCliente_ErrorValidacion() throws Exception {
        // Enviar un ClienteDto con nombre vacío
        ClienteDto clienteDto = new ClienteDto();
        clienteDto.setDni(12345678);
        clienteDto.setNombre("   ");
        clienteDto.setApellido("Pérez");
        clienteDto.setFechaNacimiento("1990-01-01");
        clienteDto.setTipoPersona("PERSONA_FISICA");
        doThrow(new IllegalArgumentException("El nombre del cliente no puede estar vacío"))
            .when(clienteValidator).validate(any(ClienteDto.class));

        // Se espera que la validación del validator lance error (o devuelva un 400)
        mockMvc.perform(post("/cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCrearCliente_UnexpectedException() throws Exception, ClienteAlreadyExistsException {
        ClienteDto clienteDto = new ClienteDto();
        clienteDto.setDni(12345678);
        clienteDto.setNombre("Juan");
        clienteDto.setApellido("Perez");
        clienteDto.setFechaNacimiento("1990-01-01");
        clienteDto.setTipoPersona("PERSONA_FISICA");
    
        doNothing().when(clienteValidator).validate(any(ClienteDto.class));
        when(clienteService.darDeAltaCliente(any(ClienteDto.class)))
                .thenThrow(new RuntimeException("Error inesperado"));
    
        mockMvc.perform(post("/cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error inesperado")));
    }
    

    @Test
    public void testBuscarCliente_Exito() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setDni(12345678);
        when(clienteService.buscarClientePorDni(12345678L))
            .thenReturn(new Cliente(new ClienteDto() {{
                setDni(12345678);
                setNombre("Juan");
                setApellido("Perez");
                setFechaNacimiento("1990-01-01");
                setTipoPersona("PERSONA_FISICA");
            }}));

        mockMvc.perform(get("/cliente/{dni}", 12345678))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(12345678));
    }

    @Test
    public void testBuscarCliente_NoExiste() throws Exception {
        when(clienteService.buscarClientePorDni(99999999L))
            .thenThrow(new IllegalArgumentException("El cliente no existe"));
    
        mockMvc.perform(get("/cliente/{dni}", 99999999))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El cliente no existe")));
    }
    

    @Test
    public void testBorrarCliente_Exito() throws Exception {
        // Para borrar, no se espera respuesta, simplemente status OK
        doNothing().when(clienteService).borrarCliente(12345678);
        mockMvc.perform(delete("/cliente/12345678"))
                .andExpect(status().isOk());
    }

    @Test
    public void testBorrarCliente_NoExiste() throws Exception {
        // Configura que al invocar borrarCliente con DNI inexistente se lance la excepción
        doThrow(new IllegalArgumentException("El cliente no existe"))
                .when(clienteService).borrarCliente(99999999L);
    
        mockMvc.perform(delete("/cliente/99999999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El cliente no existe")));
    }

    @Test
    public void testBuscarCliente_MetodoIncorrecto() throws Exception {
        mockMvc.perform(post("/cliente/12345678"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCrearCliente_JSONMalFormado() throws Exception {
        String badJson = "{ \"dni\": \"abc\", ";  // JSON incompleto/erróneo

        mockMvc.perform(post("/cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badJson))
                .andExpect(status().isBadRequest());
    }


    
}
