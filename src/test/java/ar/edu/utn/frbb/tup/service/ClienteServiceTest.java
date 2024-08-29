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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue; // Add this line
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

    //Agregar una CA$ y CC$ --> success 2 cuentas, titular peperino
    //Agregar una CA$ y CAU$S --> success 2 cuentas, titular peperino...
    //Testear clienteService.buscarPorDni

@Test
public void testAgregarCAYCCSuccess() throws TipoCuentaAlreadyExistsException {
    Cliente pepeRino = new Cliente();
    pepeRino.setDni(26456440);
    pepeRino.setNombre("Pepe");
    pepeRino.setApellido("Rino");
    pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
    pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

    Cuenta cuentaCA = new Cuenta()
            .setMoneda(TipoMoneda.PESOS)
            .setBalance(100000)
            .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

    Cuenta cuentaCC = new Cuenta()
            .setMoneda(TipoMoneda.PESOS)
            .setBalance(200000)
            .setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);

    when(clienteDao.find(26456440, true)).thenReturn(pepeRino);

    clienteService.agregarCuenta(cuentaCA, pepeRino.getDni());
    clienteService.agregarCuenta(cuentaCC, pepeRino.getDni());

    verify(clienteDao, times(2)).save(pepeRino);

    assertEquals(2, pepeRino.getCuentas().size());
    assertTrue(pepeRino.getCuentas().contains(cuentaCA));
    assertTrue(pepeRino.getCuentas().contains(cuentaCC));
    assertEquals(pepeRino, cuentaCA.getTitular());
    assertEquals(pepeRino, cuentaCC.getTitular());
}

@Test
public void testAgregarCAYCAUSDSuccess() throws TipoCuentaAlreadyExistsException {
    Cliente pepeRino = new Cliente();
    pepeRino.setDni(26456441);
    pepeRino.setNombre("Pepe");
    pepeRino.setApellido("Rino");
    pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
    pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

    Cuenta cuentaCA = new Cuenta()
            .setMoneda(TipoMoneda.PESOS)
            .setBalance(100000)
            .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

    Cuenta cuentaCAUSD = new Cuenta()
            .setMoneda(TipoMoneda.DOLARES)
            .setBalance(3000)
            .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

    when(clienteDao.find(26456441, true)).thenReturn(pepeRino);

    clienteService.agregarCuenta(cuentaCA, pepeRino.getDni());
    clienteService.agregarCuenta(cuentaCAUSD, pepeRino.getDni());

    verify(clienteDao, times(2)).save(pepeRino);

    assertEquals(2, pepeRino.getCuentas().size());
    assertTrue(pepeRino.getCuentas().contains(cuentaCA));
    assertTrue(pepeRino.getCuentas().contains(cuentaCAUSD));
    assertEquals(pepeRino, cuentaCA.getTitular());
    assertEquals(pepeRino, cuentaCAUSD.getTitular());
}

@Test
public void testBuscarPorDniCasoExito() {
    Cliente pepeRino = new Cliente();
    pepeRino.setDni(26456437);
    pepeRino.setNombre("Pepe");
    pepeRino.setApellido("Rino");
    pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
    pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

    when(clienteDao.find(26456437, true)).thenReturn(pepeRino);

    Cliente found = clienteService.buscarClientePorDni(26456437);

    assertNotNull(found);
    assertEquals(pepeRino, found);
}

@Test
public void testBuscarPorDniCasoFalla() {
    when(clienteDao.find(12345678, true)).thenReturn(null);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        clienteService.buscarClientePorDni(12345678);
    });

    assertEquals("El cliente no existe", exception.getMessage());
}}