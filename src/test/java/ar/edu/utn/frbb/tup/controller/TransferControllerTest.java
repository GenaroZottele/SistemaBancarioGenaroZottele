package ar.edu.utn.frbb.tup.controller;

import ar.edu.utn.frbb.tup.presentation.controller.TransferController;

import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;
import ar.edu.utn.frbb.tup.presentation.dto.TransferDto;
import ar.edu.utn.frbb.tup.service.TransferService;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;
import ar.edu.utn.frbb.tup.presentation.handler.TupResponseEntityExceptionHandler;

@Import(TupResponseEntityExceptionHandler.class)

@WebMvcTest(TransferController.class)
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testMakeTransfer_Exito() throws Exception {
        // Configurar un TransferDto válido
        TransferDto transferDto = new TransferDto();
        transferDto.setOrigen(1001L);
        transferDto.setDestino(2002L);
        transferDto.setMonto(500_000.0);
        transferDto.setDescripcion("Pago de servicios");
        transferDto.setMoneda(TipoMoneda.PESOS);

        // Simular que la transferencia se realiza sin problemas
        TransferEntity transferEntity = new TransferEntity();
        transferEntity.setOrigen(1001L);
        transferEntity.setDestino(2002L);
        transferEntity.setMonto(500_000.0);
        transferEntity.setTransferDate(LocalDateTime.now());
        when(transferService.makeTransfer(any(Long.class), any(Long.class), any(Double.class), any(String.class), any()))
                .thenReturn(transferEntity);

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EXITOSA"))
                .andExpect(jsonPath("$.mensaje").value("Transferencia exitosa"));
    }

    @Test
    public void testMakeTransfer_Fallo() throws Exception {
        TransferDto transferDto = new TransferDto();
        transferDto.setOrigen(1001L);
        transferDto.setDestino(2002L);
        transferDto.setMonto(500_000.0);
        transferDto.setDescripcion("Pago de servicios");
        transferDto.setMoneda(TipoMoneda.PESOS);

        // Simular que ocurre un error en la transferencia
        when(transferService.makeTransfer(any(Long.class), any(Long.class), any(Double.class), any(String.class), any()))
                .thenThrow(new IllegalArgumentException("Fondos insuficientes"));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("FALLIDA"))
                .andExpect(jsonPath("$.mensaje", containsString("Fondos insuficientes")));
    }

    @Test
    public void testGetMovements_Exito() throws Exception {
        TransferEntity transferEntity = new TransferEntity();
        transferEntity.setOrigen(1001L);
        transferEntity.setDestino(2002L);
        transferEntity.setMonto(5000.0);
        transferEntity.setTransferDate(LocalDateTime.now());

        when(transferService.getMovements(1001L)).thenReturn(Collections.singletonList(transferEntity));

        mockMvc.perform(get("/api/transfers/movements/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].monto").value(5000.0));
    }

    @Test
    public void testGetMovements_Empty() throws Exception {
        when(transferService.getMovements(9999L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/transfers/movements/9999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[]")));
    }
    @Test
    public void testMakeTransfer_ErrorResponse() throws Exception {
        TransferDto transferDto = new TransferDto();
        transferDto.setOrigen(1001L);
        transferDto.setDestino(2002L);
        transferDto.setMonto(500_000.0);
        transferDto.setDescripcion("Pago de servicios");
        transferDto.setMoneda(TipoMoneda.PESOS);
    
        // Simula un error en la transferencia
        when(transferService.makeTransfer(any(Long.class), any(Long.class), any(Double.class), any(String.class), any()))
                .thenThrow(new IllegalArgumentException("Fondos insuficientes"));
    
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value("FALLIDA"))
                .andExpect(jsonPath("$.mensaje", containsString("Fondos insuficientes")));
    }
    
}
