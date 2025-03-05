package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.persistence.TransferDao;
import ar.edu.utn.frbb.tup.persistence.entity.TransferEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransferServiceTest {

    @Mock
    private CuentaService cuentaService;

    @Mock
    private TransferDao transferDao;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private TransferService transferService;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test que verifica una transferencia exitosa entre cuentas con la misma moneda y saldo suficiente.
     */
    @Test
    public void testMakeTransfer_Exito_MismaMoneda_SaldoSuficiente() {
        // Configurar cuenta origen
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(2_000_000);
        origen.setMoneda(TipoMoneda.PESOS);
        origen.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        origen.setDniTitular(12345678L);
        // Hidratar titular de origen
        Cliente clienteOrigen = new Cliente();
        clienteOrigen.setDni(12345678L);
        origen.setTitular(clienteOrigen);

        // Configurar cuenta destino
        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setBalance(100_000);
        destino.setMoneda(TipoMoneda.PESOS);
        destino.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        destino.setDniTitular(87654321L);
        // Hidratar titular de destino
        Cliente clienteDestino = new Cliente();
        clienteDestino.setDni(87654321L);
        destino.setTitular(clienteDestino);

        // Configurar mocks para findCuentaById y buscarClientePorDni
        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);
        when(clienteService.buscarClientePorDni(12345678L)).thenReturn(clienteOrigen);
        when(clienteService.buscarClientePorDni(87654321L)).thenReturn(clienteDestino);

        // Llamar al método de transferencia
        TransferEntity result = transferService.makeTransfer(
                1001L, 2002L, 500_000.0, "Test Transfer", TipoMoneda.PESOS
        );

        // Verificar saldos: origen = 2,000,000 - 500,000 = 1,500,000, destino = 100,000 + 500,000 = 600,000
        assertEquals(1_500_000, origen.getBalance());
        assertEquals(600_000, destino.getBalance());

        // Verificar que se guarde la transferencia
        verify(transferDao, times(1)).save(any(TransferEntity.class));
        assertNotNull(result);
        assertEquals(1001L, result.getOrigen());
        assertEquals(2002L, result.getDestino());
        assertEquals(500_000.0, result.getMonto());
    }

    /**
     * Test que verifica que se lance excepción cuando la cuenta de origen no tiene fondos suficientes.
     */
    @Test
    public void testMakeTransfer_FondosInsuficientes() {
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(1000);
        origen.setMoneda(TipoMoneda.PESOS);
        origen.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        origen.setDniTitular(12345678L);
        Cliente clienteOrigen = new Cliente();
        clienteOrigen.setDni(12345678L);
        origen.setTitular(clienteOrigen);

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setBalance(5000);
        destino.setMoneda(TipoMoneda.PESOS);
        destino.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        destino.setDniTitular(87654321L);
        Cliente clienteDestino = new Cliente();
        clienteDestino.setDni(87654321L);
        destino.setTitular(clienteDestino);

        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 50_000.0, "Insuficiente", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("Fondos insuficientes"));
        verify(transferDao, never()).save(any(TransferEntity.class));
    }

    /**
     * Test que verifica que se lance excepción cuando las cuentas tienen monedas diferentes.
     */
    @Test
    public void testMakeTransfer_DistintasMonedas() {
        // Configurar cuenta origen en PESOS
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setMoneda(TipoMoneda.PESOS);
        origen.setBalance(500000);
        origen.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        origen.setDniTitular(12345678L);
        Cliente clienteOrigen = new Cliente();
        clienteOrigen.setDni(12345678L);
        origen.setTitular(clienteOrigen);

        // Configurar cuenta destino en DÓLARES
        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setMoneda(TipoMoneda.DOLARES);
        destino.setBalance(1000);
        destino.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        destino.setDniTitular(87654321L);
        Cliente clienteDestino = new Cliente();
        clienteDestino.setDni(87654321L);
        destino.setTitular(clienteDestino);

        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 10_000.0, "Moneda diferente", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("Las cuentas deben tener la misma moneda"));
        verify(transferDao, never()).save(any(TransferEntity.class));
    }

    /**
     * Test que verifica el cálculo de la comisión para transferencias en PESOS (2% para montos superiores a 1,000,000).
     */
    @Test
    public void testMakeTransfer_ComisionPesos() {
        // Configurar cuenta origen: 2,000,000 PESOS
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(2_000_000);
        origen.setMoneda(TipoMoneda.PESOS);
        origen.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        origen.setDniTitular(12345678L);
        Cliente clienteOrigen = new Cliente();
        clienteOrigen.setDni(12345678L);
        origen.setTitular(clienteOrigen);

        // Configurar cuenta destino: 0 PESOS
        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setBalance(0);
        destino.setMoneda(TipoMoneda.PESOS);
        destino.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        destino.setDniTitular(87654321L);
        Cliente clienteDestino = new Cliente();
        clienteDestino.setDni(87654321L);
        destino.setTitular(clienteDestino);

        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);
        when(clienteService.buscarClientePorDni(12345678L)).thenReturn(clienteOrigen);
        when(clienteService.buscarClientePorDni(87654321L)).thenReturn(clienteDestino);

        transferService.makeTransfer(1001L, 2002L, 1_200_000.0, "Comision Pesos", TipoMoneda.PESOS);

        // Se debitan 1,200,000 + 2% = 1,224,000; saldo final en origen = 2,000,000 - 1,224,000 = 776,000.
        // En destino se acreditan 1,200,000; saldo final en destino = 0 + 1,200,000 = 1,200,000.
        assertEquals(776_000, origen.getBalance());
        assertEquals(1_200_000, destino.getBalance());
    }

    /**
     * Test que verifica el cálculo de la comisión para transferencias en DÓLARES (0.5% para montos superiores a 5,000).
     */
    @Test
    public void testMakeTransfer_ComisionDolares() {
        // Configurar cuenta origen: 10,000 DÓLARES
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(10_000);
        origen.setMoneda(TipoMoneda.DOLARES);
        origen.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        origen.setDniTitular(12345678L);
        Cliente clienteOrigen = new Cliente();
        clienteOrigen.setDni(12345678L);
        origen.setTitular(clienteOrigen);

        // Configurar cuenta destino: 100 DÓLARES
        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setBalance(100);
        destino.setMoneda(TipoMoneda.DOLARES);
        destino.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        destino.setDniTitular(87654321L);
        Cliente clienteDestino = new Cliente();
        clienteDestino.setDni(87654321L);
        destino.setTitular(clienteDestino);

        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);
        when(clienteService.buscarClientePorDni(12345678L)).thenReturn(clienteOrigen);
        when(clienteService.buscarClientePorDni(87654321L)).thenReturn(clienteDestino);

        transferService.makeTransfer(1001L, 2002L, 6_000.0, "Comision Dolares", TipoMoneda.DOLARES);

        // 6,000 + 0.5% = 6,030; origen final = 10,000 - 6,030 = 3,970; destino final = 100 + 6,000 = 6,100.
        assertEquals(3970, origen.getBalance());
        assertEquals(6100, destino.getBalance());
    }

    /**
     * Test que verifica que se lance excepción si la cuenta destino no existe.
     */
    @Test
    public void testMakeTransfer_CuentaDestinoNoExiste() {
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(500000);
        origen.setMoneda(TipoMoneda.PESOS);
        origen.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        origen.setDniTitular(12345678L);
        Cliente clienteOrigen = new Cliente();
        clienteOrigen.setDni(12345678L);
        origen.setTitular(clienteOrigen);
        
        // Simular que la cuenta destino no existe (retorna null)
        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(9999L)).thenReturn(null);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 9999L, 10_000.0, "Destino no existe", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().toLowerCase().contains("no existe"));
        verify(transferDao, never()).save(any(TransferEntity.class));
    }

    /**
     * Test que verifica la obtención exitosa de movimientos para una cuenta.
     */
    @Test
    public void testGetMovements_Success() {
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

    /**
     * Test que verifica que se retorne una lista vacía de movimientos si no existen transferencias para la cuenta.
     */
    @Test
    public void testGetMovements_Empty() {
        when(transferDao.findAllByCuenta(9999L)).thenReturn(Collections.emptyList());
        List<TransferEntity> resultado = transferService.getMovements(9999L);
        assertTrue(resultado.isEmpty());
    }

    /**
     * Test que verifica que se lance excepción si el ID de la cuenta de origen es inválido.
     */
    @Test
    public void testMakeTransfer_OrigenInvalido() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(0L, 2002L, 100.0, "Origen inválido", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("origen debe ser un ID válido"));
        verify(transferDao, never()).save(any());
    }

    /**
     * Test que verifica que se lance excepción si el ID de la cuenta destino es inválido.
     */
    @Test
    public void testMakeTransfer_DestinoInvalido() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, -1L, 100.0, "Destino inválido", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("destino debe ser un ID válido"));
        verify(transferDao, never()).save(any());
    }

    /**
     * Test que verifica que se lance excepción cuando el origen y destino son la misma cuenta.
     */
    @Test
    public void testMakeTransfer_MismoOrigenYDestino() {
        // Configurar una única cuenta
        Cuenta mismaCuenta = new Cuenta();
        mismaCuenta.setNumeroCuenta(1001L);
        mismaCuenta.setBalance(2_000_000);
        mismaCuenta.setMoneda(TipoMoneda.PESOS);
        mismaCuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        mismaCuenta.setDniTitular(12345678L);
        Cliente cliente = new Cliente();
        cliente.setDni(12345678L);
        mismaCuenta.setTitular(cliente);
        
        when(cuentaService.findCuentaById(1001L)).thenReturn(mismaCuenta);
        when(clienteService.buscarClientePorDni(12345678L)).thenReturn(cliente);
        
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 1001L, 500.0, "Mismo Origen-Destino", TipoMoneda.PESOS)
        );
        // Ajustar la subcadena a lo que se lanza en el mensaje
        assertTrue(e.getMessage().toLowerCase().contains("origen y destino"));
        verify(transferDao, never()).save(any(TransferEntity.class));
    }

    /**
     * Test que verifica que se lance excepción si el monto a transferir es cero o negativo.
     */
    @Test
    public void testMakeTransfer_MontoCeroONegativo() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 0.0, "Monto cero", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("El monto a transferir debe ser mayor a 0"));
        verify(transferDao, never()).save(any());
    }

    /**
     * Test que verifica que se lance excepción si la descripción de la transferencia es vacía.
     */
    @Test
    public void testMakeTransfer_DescripcionVacia() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 100.0, "   ", TipoMoneda.PESOS)
        );
        assertTrue(e.getMessage().contains("La descripción no puede estar vacía"));
        verify(transferDao, never()).save(any());
    }

    /**
     * Test que verifica que se lance excepción si la moneda es nula.
     */
    @Test
    public void testMakeTransfer_MonedaNula() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                transferService.makeTransfer(1001L, 2002L, 100.0, "Moneda nula", null)
        );
        assertTrue(e.getMessage().contains("La moneda no puede ser nula"));
        verify(transferDao, never()).save(any());
    }

    @Test
    void testMakeTransfer_SinComision_Pesos() {
        // Configurar cuentas donde el monto es exactamente 1,000,000 PESOS (sin comisión)
        Cuenta origen = new Cuenta();
        origen.setNumeroCuenta(1001L);
        origen.setBalance(2_000_000);
        origen.setMoneda(TipoMoneda.PESOS);
        origen.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        origen.setDniTitular(12345678L);
        Cliente clienteOrigen = new Cliente();
        clienteOrigen.setDni(12345678L);
        origen.setTitular(clienteOrigen);

        Cuenta destino = new Cuenta();
        destino.setNumeroCuenta(2002L);
        destino.setBalance(100_000);
        destino.setMoneda(TipoMoneda.PESOS);
        destino.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        destino.setDniTitular(87654321L);
        Cliente clienteDestino = new Cliente();
        clienteDestino.setDni(87654321L);
        destino.setTitular(clienteDestino);

        when(cuentaService.findCuentaById(1001L)).thenReturn(origen);
        when(cuentaService.findCuentaById(2002L)).thenReturn(destino);
        when(clienteService.buscarClientePorDni(12345678L)).thenReturn(clienteOrigen);
        when(clienteService.buscarClientePorDni(87654321L)).thenReturn(clienteDestino);

        TransferEntity result = transferService.makeTransfer(1001L, 2002L, 1_000_000.0, "Sin comisión", TipoMoneda.PESOS);

        // Sin comisión, se debitan 1,000,000 y se acreditan 1,000,000
        assertEquals(1_000_000, origen.getBalance());
        assertEquals(1_100_000, destino.getBalance());
        verify(transferDao, times(1)).save(any(TransferEntity.class));
    }

}
