package ar.edu.utn.frbb.tup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaNotSupportedException;
import ar.edu.utn.frbb.tup.presentation.controller.CuentaController;
import ar.edu.utn.frbb.tup.service.CuentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;
import ar.edu.utn.frbb.tup.presentation.handler.TupResponseEntityExceptionHandler;

@Import(TupResponseEntityExceptionHandler.class)
@WebMvcTest(CuentaController.class)
public class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CuentaService cuentaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCrearCuenta_Exito() throws Exception, CuentaAlreadyExistsException {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1001L);
        cuenta.setBalance(500000);
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setMoneda(TipoMoneda.PESOS);
        // Suponemos que la creación se realizó correctamente.
        when(cuentaService.darDeAltaCuenta(any(Cuenta.class), anyLong())).thenReturn(cuenta);

        mockMvc.perform(post("/cuenta/12345678")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuenta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value(1001L));
    }

    @Test
    public void testAgregarBalance_Exito() throws Exception {
        // Simular que agregar balance se realiza sin problemas.
        doNothing().when(cuentaService).agregarBalance(1001L, 10000);

        mockMvc.perform(post("/cuenta/1001/agregar-balance")
                .param("monto", "10000"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Balance agregado exitosamente")));
    }

    @Test
    public void testAgregarBalance_Error() throws Exception {
        // Simular que agregar balance con monto negativo arroja error.
        doThrow(new IllegalArgumentException("El monto a agregar no puede ser negativo."))
                .when(cuentaService).agregarBalance(1001L, -500);

        mockMvc.perform(post("/cuenta/1001/agregar-balance")
                .param("monto", "-500"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("no puede ser negativo")));
    }

    @Test
    public void testFindCuentaById_Exito() throws Exception {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1001L);
        // Configurar otros campos según corresponda
        when(cuentaService.findCuentaById(1001L)).thenReturn(cuenta);

        mockMvc.perform(get("/cuenta/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value(1001L));
    }

    @Test
    public void testGetCuentasByCliente_Exito() throws Exception {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1001L);
        when(cuentaService.getCuentasByCliente(12345678L)).thenReturn(Collections.singletonList(cuenta));

        mockMvc.perform(get("/cuenta/cliente/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value(1001L));
    }

    @Test
    public void testCrearCuenta_Falla_CuentaYaExiste() throws Exception, CuentaAlreadyExistsException {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1001L);
        cuenta.setBalance(500000);
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setMoneda(TipoMoneda.PESOS);
        
        // Simulamos que al intentar dar de alta la cuenta se lanza excepción
        when(cuentaService.darDeAltaCuenta(any(Cuenta.class), anyLong()))
                .thenThrow(new CuentaAlreadyExistsException("La cuenta ya existe"));

        mockMvc.perform(post("/cuenta/12345678")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuenta)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("La cuenta ya existe")));
    }

    @Test
    public void testCrearCuenta_Falla_TipoNoSoportado() throws Exception, CuentaAlreadyExistsException {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1002L);
        cuenta.setBalance(500000);
        cuenta.setTipoCuenta(TipoCuenta.NO_SOPORTADA);  // Tipo no soportado
        cuenta.setMoneda(TipoMoneda.PESOS);
        
        when(cuentaService.darDeAltaCuenta(any(Cuenta.class), anyLong()))
                .thenThrow(new TipoCuentaNotSupportedException("El tipo de cuenta no es soportado"));

        mockMvc.perform(post("/cuenta/12345678")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuenta)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El tipo de cuenta no es soportado")));
    }

}
