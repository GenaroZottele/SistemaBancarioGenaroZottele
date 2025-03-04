package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.persistence.TransferDao;
import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransferServiceTest {

    @Mock
    private CuentaService cuentaService;

    @Mock
    private TransferDao transferDao;

    @InjectMocks
    private TransferService transferService;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMakeTransfer_Exito_MismaMoneda_SaldoSuficiente() {
        // 1) Mockear cuenta origen con saldo alto, moneda PESOS
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(2_000_000);
        origen.setMoneda(TipoMoneda.PESOS);

        // 2) Mockear cuenta destino con la misma moneda
        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setBalance(100_000);
        destino.setMoneda(TipoMoneda.PESOS);

        // 3) Cuando busque estas cuentas:
        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);

        // 4) Llamar al método
        TransferEntity result = transferService.makeTransfer(
                1001L, 2002L, 500_000.0, "Test Transfer", TipoMoneda.PESOS
        );

        // 5) Verificar la lógica de saldo y comisiones (monto=500k, <1M => no hay 2%).
        //    Esperamos que el saldo de origen sea 2.000.000 - 500.000 = 1.500.000
        //    y el destino sea 600.000
        assertEquals(1_500_000, origen.getBalance());
        assertEquals(600_000, destino.getBalance());

        // 6) Verificar que se guarde la TransferEntity
        verify(transferDao, times(1)).save(any(TransferEntity.class));
        assertNotNull(result);
        assertEquals(1001L, result.getOrigen());
        assertEquals(2002L, result.getDestino());
        assertEquals(500_000.0, result.getMonto());
    }

    @Test
    public void testMakeTransfer_FondosInsuficientes() {
        // Origen con saldo muy bajo
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(1000);
        origen.setMoneda(TipoMoneda.PESOS);

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setBalance(5000);
        destino.setMoneda(TipoMoneda.PESOS);

        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);

        // Esperamos que lance excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 50_000.0, "Insuficiente", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("Fondos insuficientes"));
        // Verificar que no se llame a transferDao.save
        verify(transferDao, never()).save(any(TransferEntity.class));
    }

    @Test
    public void testMakeTransfer_DistintasMonedas() {
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setMoneda(TipoMoneda.PESOS);
        origen.setBalance(500000);

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setMoneda(TipoMoneda.DOLARES);
        destino.setBalance(1000);

        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 10_000.0, "Moneda diferente", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("Las cuentas deben tener la misma moneda"));
        verify(transferDao, never()).save(any(TransferEntity.class));
    }

    @Test
    public void testMakeTransfer_ComisionPesos() {
        // >1.000.000 en pesos => 2% de 1.200.000 = 24.000
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(2_000_000);
        origen.setMoneda(TipoMoneda.PESOS);

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setBalance(0);
        destino.setMoneda(TipoMoneda.PESOS);

        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);

        transferService.makeTransfer(1001L, 2002L, 1_200_000.0, "Comision Pesos", TipoMoneda.PESOS);

        // Debita 1.224.000 (monto + 2%) => saldo final = 776.000
        assertEquals(776_000, origen.getBalance());
        // A destino le suman 1.200.000
        assertEquals(1_200_000, destino.getBalance());
    }

    @Test
    public void testMakeTransfer_ComisionDolares() {
        // >5.000 en dolares => 0.5%
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(10_000);
        origen.setMoneda(TipoMoneda.DOLARES);

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setBalance(100);
        destino.setMoneda(TipoMoneda.DOLARES);

        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);

        transferService.makeTransfer(1001L, 2002L, 6_000.0, "Comision Dolares", TipoMoneda.DOLARES);

        // 6.000 + 0.5% = 6.030 => origen = 10.000 - 6.030 = 3.970
        // destino = 100 + 6.000 = 6.100
        assertEquals(3970, origen.getBalance());
        assertEquals(6100, destino.getBalance());
    }

    @Test
    public void testMakeTransfer_CuentaDestinoNoExiste() {
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(500000);
        origen.setMoneda(TipoMoneda.PESOS);

        // Origen OK, destino null => Se simula "otro banco" o error
        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(9999L)).thenReturn(null);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 9999L, 10_000.0, "Destino no existe", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("no existe"));
        verify(transferDao, never()).save(any(TransferEntity.class));
    }

    @Test
    public void testGetMovements_Success() {
        // Mockear la lista de TransferEntity
        List<TransferEntity> mockTransfers = new ArrayList<>();
        TransferEntity t1 = new TransferEntity();
        t1.setOrigen(1001L);
        t1.setDestino(2002L);
        t1.setMonto(5000.0);
        t1.setTransferDate(LocalDateTime.now());
        mockTransfers.add(t1);

        when(transferDao.findAllByCuenta(1001L)).thenReturn(mockTransfers);

        List<TransferEntity> resultado = transferService.getMovements(1001L);
        assertEquals(1, resultado.size());
        assertEquals(5000.0, resultado.get(0).getMonto());
    }

    @Test
    public void testGetMovements_Empty() {
        when(transferDao.findAllByCuenta(9999L)).thenReturn(Collections.emptyList());
        List<TransferEntity> resultado = transferService.getMovements(9999L);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testMakeTransfer_OrigenInvalido() {
        // Comportamiento: si origen <= 0, se lanza excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(0L, 2002L, 100.0, "Origen inválido", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("origen debe ser un ID válido"));
        verify(transferDao, never()).save(any());
}

    @Test
    public void testMakeTransfer_DestinoInvalido() {
        // Comportamiento: si destino <= 0, se lanza excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, -1L, 100.0, "Destino inválido", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("destino debe ser un ID válido"));
        verify(transferDao, never()).save(any());
    }

    @Test
    public void testMakeTransfer_MismoOrigenYDestino() {
        // Origen y destino iguales => no debe permitir
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 1001L, 500.0, "Mismo Origen-Destino", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("origen y destino no pueden ser la misma cuenta"));
        verify(transferDao, never()).save(any());
    }

    @Test
    public void testMakeTransfer_MontoCeroONegativo() {
        // Monto <= 0 => excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 0.0, "Monto cero", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("El monto a transferir debe ser mayor a 0"));
        verify(transferDao, never()).save(any());
    }

    @Test
    public void testMakeTransfer_DescripcionVacia() {
        // Descripcion vacía => excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 100.0, "   ", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("La descripción no puede estar vacía"));
        verify(transferDao, never()).save(any());
    }

    @Test
    public void testMakeTransfer_MonedaNula() {
        // Moneda null => excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 100.0, "Moneda nula", null)
        );
        assertTrue(e.getMessage().contains("La moneda no puede ser nula"));
        verify(transferDao, never()).save(any());
    }

}
