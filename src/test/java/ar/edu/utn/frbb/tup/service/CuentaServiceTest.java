package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.*;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaNotSupportedException;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CuentaServiceTest {

    @Mock
    private CuentaDao cuentaDao;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private CuentaService cuentaService;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCuentaExistente() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoCuentaNotSupportedException {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1L);
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setMoneda(TipoMoneda.PESOS);
        cuenta.setBalance(500000);

        when(cuentaDao.find(1L)).thenReturn(cuenta);

        assertThrows(CuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta, 12345678));
    }

    @Test
    public void testCuentaNoSoportada() throws TipoCuentaNotSupportedException, CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(2L);
        cuenta.setTipoCuenta(TipoCuenta.NO_SOPORTADA); 
        cuenta.setMoneda(TipoMoneda.PESOS);
        cuenta.setBalance(500000);

        assertThrows(TipoCuentaNotSupportedException.class, () -> cuentaService.darDeAltaCuenta(cuenta, 12345678));
    }

    @Test
    public void testClienteYaTieneCuentaDeEseTipo() throws TipoCuentaAlreadyExistsException {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(3L);
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setMoneda(TipoMoneda.PESOS);
        cuenta.setBalance(500000);

        doThrow(TipoCuentaAlreadyExistsException.class).when(clienteService).agregarCuenta(cuenta, 12345678);

        assertThrows(TipoCuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta, 12345678));
    }

    @Test
    public void testCuentaCreadaExitosamente() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, TipoCuentaNotSupportedException {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(4L);
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setMoneda(TipoMoneda.PESOS);
        cuenta.setBalance(500000);

        when(cuentaDao.find(4L)).thenReturn(null);
        doNothing().when(clienteService).agregarCuenta(cuenta, 12345678);

        cuentaService.darDeAltaCuenta(cuenta, 12345678);

        verify(cuentaDao, times(1)).save(cuenta);
        verify(clienteService, times(1)).agregarCuenta(cuenta, 12345678);
    }

    @Test
    public void testFindCuentaById_Existe() {
        Cuenta mockCuenta = new Cuenta();
        mockCuenta.setNumeroCuenta(1001L);
        mockCuenta.setDniTitular(12345678L);

        // simulamos que la DAO retorna la cuenta sin titular
        when(cuentaDao.find(1001L)).thenReturn(mockCuenta);

        // simulamos que el cliente existe en clienteService
        Cliente mockCliente = new Cliente();
        mockCliente.setDni(12345678L);
        when(clienteService.buscarClientePorDni(12345678L)).thenReturn(mockCliente);

        Cuenta result = cuentaService.findCuentaById(1001L);

        assertNotNull(result);
        assertEquals(1001L, result.getNumeroCuenta());
        assertNotNull(result.getTitular()); // se hidrata
        assertEquals(12345678L, result.getTitular().getDni());

        verify(cuentaDao, times(1)).find(1001L);
        verify(clienteService, times(1)).buscarClientePorDni(12345678L);
    }

    @Test
    public void testFindCuentaById_NoExiste() {
        when(cuentaDao.find(9999L)).thenReturn(null);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                cuentaService.findCuentaById(9999L)
        );
        assertTrue(e.getMessage().contains("No se encontró la cuenta"));
    }

    @Test
    public void testAgregarBalance_Success() {
        // No hay mucho que mockear, solo la llamada a cuentaDao
        doNothing().when(cuentaDao).agregarBalance(1001L, 5000);

        // Llamamos
        cuentaService.agregarBalance(1001L, 5000);

        // Verificamos la llamada
        verify(cuentaDao, times(1)).agregarBalance(1001L, 5000);
    }

    @Test
    public void testAgregarBalance_Negativo() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                cuentaService.agregarBalance(1001L, -300)
        );
        assertTrue(e.getMessage().contains("no puede ser negativo"));
        verify(cuentaDao, never()).agregarBalance(anyLong(), anyInt());
    }

    @Test
    public void testGetCuentasByCliente_Exito() {
        List<Cuenta> lista = new ArrayList<>();
        Cuenta c1 = new Cuenta();
        c1.setNumeroCuenta(10L);
        lista.add(c1);

        when(cuentaDao.getCuentasByCliente(1234L)).thenReturn(lista);

        List<Cuenta> resultado = cuentaService.getCuentasByCliente(1234L);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());

        verify(cuentaDao, times(1)).getCuentasByCliente(1234L);
    }

    @Test
    public void testGetCuentasByCliente_Empty() {
        when(cuentaDao.getCuentasByCliente(7777L)).thenReturn(new ArrayList<>());

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                cuentaService.getCuentasByCliente(7777L)
        );
        assertTrue(e.getMessage().contains("No se encontraron cuentas"));
    }

    @Test
    public void testDarDeAltaCuenta_NullCuenta() {
        // Llamamos a darDeAltaCuenta con cuenta = null
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                cuentaService.darDeAltaCuenta(null, 12345678L)
        );
        assertTrue(e.getMessage().contains("La cuenta no puede ser nula"));
        // Verificamos que nunca se llame a cuentaDao.save
        verify(cuentaDao, never()).save(any(Cuenta.class));
}

    ///////////////////////////////////////
    // 2) Test darDeAltaCuenta con moneda nula
    ///////////////////////////////////////
    @Test
    public void testDarDeAltaCuenta_MonedaNula() {
        // Creamos una cuenta sin moneda
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(10L);
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setBalance(1000);

        // Se espera que lance excepción por moneda nula
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                cuentaService.darDeAltaCuenta(cuenta, 12345678L)
        );
        assertTrue(e.getMessage().contains("La moneda de la cuenta no puede ser nula"));
        verify(cuentaDao, never()).save(any(Cuenta.class));
    }

    ///////////////////////////////////////
    // 3) Test findCuentaById con ID inválido
    ///////////////////////////////////////
    @Test
    public void testFindCuentaById_IdInvalido() {
        // ID = 0 o negativo => Excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                cuentaService.findCuentaById(0L)
        );
        assertTrue(e.getMessage().contains("El ID de la cuenta debe ser mayor a 0"));
        verify(cuentaDao, never()).find(anyLong());
    }

    ///////////////////////////////////////
    // 4) Test getCuentasByCliente con DNI inválido
    ///////////////////////////////////////
    @Test
    public void testGetCuentasByCliente_DniInvalido() {
        // dni = 0 => excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                cuentaService.getCuentasByCliente(0L)
        );
        assertTrue(e.getMessage().contains("El DNI del cliente debe ser mayor a 0"));
        // Verificar que no llame a cuentaDao
        verify(cuentaDao, never()).getCuentasByCliente(anyLong());
    }

    ///////////////////////////////////////
    // 5) Test agregarBalance con ID inválido
    ///////////////////////////////////////
    @Test
    public void testAgregarBalance_IdInvalido() {
        // Si el ID de la cuenta es <= 0 => excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                cuentaService.agregarBalance(0L, 500)
        );
        assertTrue(e.getMessage().contains("El ID de la cuenta debe ser mayor a 0"));
        verify(cuentaDao, never()).agregarBalance(anyLong(), anyInt());
    }
}

