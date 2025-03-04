package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.presentation.dto.ClienteDto;
import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.model.TipoPersona;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClienteServiceTest {

    @Mock
    private ClienteDao clienteDao;

    @InjectMocks
    private ClienteService clienteService;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testClienteMenor18Años() {
        ClienteDto clienteMenorDeEdad = new ClienteDto();
        clienteMenorDeEdad.setFechaNacimiento("2020-03-18");
        assertThrows(IllegalArgumentException.class, () -> clienteService.darDeAltaCliente(clienteMenorDeEdad));
    }

    @Test
    public void testClienteSuccess() throws ClienteAlreadyExistsException {
        ClienteDto cliente = new ClienteDto();
        cliente.setFechaNacimiento("1978-03-18");
        cliente.setDni(29857643);
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA.toString());
        Cliente clienteEntity = clienteService.darDeAltaCliente(cliente);

        verify(clienteDao, times(1)).save(clienteEntity);
    }

    @Test
    public void testClienteAlreadyExistsException() throws ClienteAlreadyExistsException {
        ClienteDto pepeRino = new ClienteDto();
        pepeRino.setDni(26456437);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento("1978-03-18");
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA.toString());

        when(clienteDao.find(26456437, false)).thenReturn(new Cliente());

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.darDeAltaCliente(pepeRino));
    }



    @Test
    public void testAgregarCuentaAClienteSuccess() throws TipoCuentaAlreadyExistsException {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456439);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(26456439, true)).thenReturn(pepeRino);

        clienteService.agregarCuenta(cuenta, pepeRino.getDni());

        verify(clienteDao, times(1)).save(pepeRino);

        assertEquals(1, pepeRino.getCuentas().size());
        assertEquals(pepeRino, cuenta.getTitular());

    }


    @Test
    public void testAgregarCuentaAClienteDuplicada() throws TipoCuentaAlreadyExistsException {
        Cliente luciano = new Cliente();
        luciano.setDni(26456439);
        luciano.setNombre("Pepe");
        luciano.setApellido("Rino");
        luciano.setFechaNacimiento(LocalDate.of(1978, 3,25));
        luciano.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(26456439, true)).thenReturn(luciano);

        clienteService.agregarCuenta(cuenta, luciano.getDni());

        Cuenta cuenta2 = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        assertThrows(TipoCuentaAlreadyExistsException.class, () -> clienteService.agregarCuenta(cuenta2, luciano.getDni()));
        verify(clienteDao, times(1)).save(luciano);
        assertEquals(1, luciano.getCuentas().size());
        assertEquals(luciano, cuenta.getTitular());

    }

        @Test
    public void testBuscarClientePorDni_Existe() {
        // Configurar mock
        Cliente mockCliente = new Cliente();
        mockCliente.setDni(12345678);
        when(clienteDao.find(12345678L, true)).thenReturn(mockCliente);

        // Llamar al método
        Cliente resultado = clienteService.buscarClientePorDni(12345678L);

        // Verificar
        assertNotNull(resultado);
        assertEquals(12345678, resultado.getDni());
        verify(clienteDao, times(1)).find(12345678L, true);
    }

    @Test
    public void testBuscarClientePorDni_NoExiste() {
        when(clienteDao.find(99999999L, true)).thenReturn(null);

        // Esperamos que lance IllegalArgumentException
        Exception e = assertThrows(IllegalArgumentException.class, () -> 
                clienteService.buscarClientePorDni(99999999L)
        );
        assertTrue(e.getMessage().contains("El cliente no existe"));
    }

    @Test
    public void testBorrarCliente_Exito() {
        // El cliente existe en la DB
        Cliente mockCliente = new Cliente();
        mockCliente.setDni(55555555);
        when(clienteDao.find(55555555L, false)).thenReturn(mockCliente);

        // Llamar al método
        clienteService.borrarCliente(55555555L);

        // Verificar que se llame a delete
        verify(clienteDao, times(1)).delete(55555555L);
    }

    @Test
    public void testBorrarCliente_NoExiste() {
        // No se encuentra en la DB
        when(clienteDao.find(77777777L, false)).thenReturn(null);

        // Esperamos que lance excepción
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                clienteService.borrarCliente(77777777L)
        );
        assertTrue(e.getMessage().contains("El cliente no existe"));
    }

    @Test
    public void testClienteJusto18Anios() throws ClienteAlreadyExistsException {
        // Si hoy es 2025-01-01, una persona nacida en 2007-01-01 cumpliría recién 18. 
        // Ajusta la fecha según tu lógica
        ClienteDto clienteMayor = new ClienteDto();
        clienteMayor.setDni(123);
        clienteMayor.setFechaNacimiento("2005-02-27"); // simula que hoy cumple 20, ajusta la fecha al test real

        // Asumimos no existe
        when(clienteDao.find(123, false)).thenReturn(null);

        // No debería lanzar excepción si ya tiene >=18
        assertDoesNotThrow(() -> clienteService.darDeAltaCliente(clienteMayor));
        verify(clienteDao, times(1)).save(any(Cliente.class));
    }

    ////////////////////////////////////////
    // 1) Dar de alta cliente con null
    ////////////////////////////////////////
    @Test
    public void testDarDeAltaCliente_NullDto() {
        // Si pasamos null como clienteDto, esperamos que lance IllegalArgumentException
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                clienteService.darDeAltaCliente(null)
        );
        assertTrue(e.getMessage().contains("no puede ser nulo")); // Ajustá el mensaje
        // Verificamos que no se llame a clienteDao
        verify(clienteDao, never()).save(any());
    }

    ////////////////////////////////////////
    // 2) Dar de alta cliente con DNI inválido
    ////////////////////////////////////////
    @Test
    public void testDarDeAltaCliente_DniInvalido() {
        // Creamos un ClienteDto con DNI <= 0
        ClienteDto clienteDto = new ClienteDto();
        clienteDto.setDni(0);
        clienteDto.setFechaNacimiento("1990-05-10");

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                clienteService.darDeAltaCliente(clienteDto)
        );
        assertTrue(e.getMessage().contains("El DNI del cliente debe ser mayor a 0")); 
        verify(clienteDao, never()).save(any());
    }

    ////////////////////////////////////////
    // 3) Dar de alta cliente con nombre vacío
    ////////////////////////////////////////
    @Test
    public void testDarDeAltaCliente_NombreVacio() {
        // Asumimos que tu lógica chequea nombre / apellido vacíos
        ClienteDto clienteDto = new ClienteDto();
        clienteDto.setDni(123456);
        clienteDto.setNombre("   ");  // nombre vacío
        clienteDto.setApellido("Rios");
        clienteDto.setFechaNacimiento("1980-01-01");

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                clienteService.darDeAltaCliente(clienteDto)
        );
        assertTrue(e.getMessage().contains("El nombre del cliente no puede estar vacío"));
        verify(clienteDao, never()).save(any());
    }

    ////////////////////////////////////////
    // 4) Agregar cuenta con cuenta nula
    ////////////////////////////////////////
    @Test
    public void testAgregarCuenta_NullCuenta() throws TipoCuentaAlreadyExistsException {
        // Llamamos a agregarCuenta con cuenta=null
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                clienteService.agregarCuenta(null, 26456439)
        );
        assertTrue(e.getMessage().contains("La cuenta no puede ser nula"));
        verify(clienteDao, never()).save(any());
    }

    ////////////////////////////////////////
    // 5) Agregar cuenta a cliente con DNI inválido
    ////////////////////////////////////////
    @Test
    public void testAgregarCuenta_DniInvalido() throws TipoCuentaAlreadyExistsException {
        // DNI <= 0 => excepción
        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setMoneda(TipoMoneda.PESOS);

        Exception e = assertThrows(IllegalArgumentException.class, () ->
                clienteService.agregarCuenta(cuenta, 0)
        );
        assertTrue(e.getMessage().contains("El DNI del titular debe ser mayor a 0"));
        verify(clienteDao, never()).find(anyLong(), anyBoolean());
    }

}