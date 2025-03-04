# Sistema Bancario - Proyecto Final

Este proyecto consiste en un sistema bancario simplificado que permite administrar clientes, cuentas y transferencias, todo desarrollado con Spring Boot y persistido en una “base de datos” en memoria (utilizando mapas) para mayor sencillez. A continuación se describe de manera completa y detallada en qué consiste cada capa del proyecto, el flujo de datos de punta a punta y, al final, cómo se pueden testear los distintos endpoints.

---

## 1. Descripción General

El sistema bancario está diseñado para:
1. **Administrar clientes** (alta, baja, búsqueda).
2. **Administrar cuentas** para cada cliente (altas, consulta de saldos, etc.).
3. **Realizar transferencias** entre cuentas (ya sean del mismo cliente, de distintos clientes o incluso “simulando” cuentas de otros bancos).
4. **Llevar un registro** de cada operación y retornar historiales de transacciones cuando se requiera.

Aunque la persistencia real está hecha a través de **objetos en memoria**, la estructura del proyecto y la arquitectura siguen el patrón típico de **capas** (Controlador, Servicio, DAO, Modelo/Entidad). Esto permite que, en un futuro, sea sencillo integrar una base de datos real o servicios externos.

---

## 2. Estructura del Proyecto

El proyecto está dividido lógicamente en **capas**. A continuación se muestra la descripción de cada una:

### 2.1 Capa de Presentación (Controladores)

- **ClienteController**: expone los endpoints para manejar la creación de clientes, búsqueda por DNI, borrado y otras operaciones relacionadas.
- **CuentaController**: maneja la creación de cuentas asociadas a un cliente, la obtención de cuentas por cliente, la consulta de cuentas por ID y la acción de agregar saldo a una cuenta.
- **TransferController**: gestiona las solicitudes de transferencia de fondos entre cuentas y la obtención de movimientos (historial de transferencias).

Estos controladores reciben y devuelven datos en formato **JSON**, y se encargan de interactuar con la capa de servicios para aplicar la lógica de negocio.

### 2.2 Capa de Servicio

- **ClienteService**: contiene la lógica para dar de alta un nuevo cliente, verificar si un cliente existe y si tiene la edad suficiente, gestionar las cuentas de un cliente y eliminar clientes si fuese necesario.
- **CuentaService**: se encarga de validar y crear cuentas para un cliente específico, asegurándose de que no existan duplicados de tipo y moneda, manejando los saldos, y facilitando la búsqueda de cuentas por ID o por DNI de su titular.
- **TransferService**: aplica las reglas de negocio para realizar transferencias:
  - Verifica que ambas cuentas existan.
  - Comprueba que sean de la misma moneda y que el saldo sea suficiente.
  - Calcula comisiones (por encima de ciertos montos) según sea en pesos o dólares.
  - Actualiza los saldos en las cuentas origen y destino.
  - Registra la transferencia en un “DAO” especial para consultarla más adelante.

La capa de servicio centraliza las validaciones y reglas de negocio antes de llamar a la capa de persistencia.

### 2.3 Capa de Persistencia (DAOs)

Esta capa se construye sobre una “base de datos en memoria” (un `Map` por entidad), y cada DAO se encarga de una entidad específica:

- **ClienteDao**:
  - Guarda y recupera clientes (`ClienteEntity`) en el mapa de memoria.
  - Permite buscar clientes por DNI y, si se especifica, cargar también las cuentas asociadas (usando `CuentaDao`).
- **CuentaDao**:
  - Guarda y recupera cuentas (`CuentaEntity`).
  - Ofrece la búsqueda de cuentas por ID y también por DNI de su titular (para “cargar” todas las cuentas de un cliente).
  - Contiene un método para modificar el saldo de una cuenta (`agregarBalance`).
- **TransferDao**:
  - Guarda y recupera transferencias (`TransferEntity`).
  - Permite filtrar transferencias por la cuenta implicada, para construir el historial de movimientos.

Estos DAOs, a pesar de operar en memoria, están diseñados como si fuesen repositorios hacia un sistema real, facilitando la sustitución futura por JPA/Hibernate o cualquier otro motor de persistencia.

### 2.4 Modelo (Entidades de Negocio)

- **Cliente**: contiene los datos del cliente (DNI, nombre, apellido, fecha de nacimiento, etc.), su tipo de persona (física o jurídica), la lista de cuentas, etc.  
- **Cuenta**: representa la cuenta bancaria, con su número de cuenta (`numeroCuenta`), saldo (`balance`), tipo de cuenta (CAJA_AHORRO, CUENTA_CORRIENTE) y la moneda (PESOS o DOLARES).  
- **Transfer** (y `TransferEntity`): describe una transferencia realizada, guardando origen, destino, monto, descripción, fecha y moneda.

En esta capa también residen **excepciones personalizadas** (por ejemplo, `ClienteAlreadyExistsException`, `TipoCuentaAlreadyExistsException`, `CantidadNegativaException`, etc.) que ayudan a manejar las validaciones y errores de negocio.

---

## 3. Flujo de Datos de Punta a Punta

Para entender mejor cómo viajan los datos desde la petición de un cliente hasta la respuesta final, veamos un caso de ejemplo: **Crear una nueva cuenta para un cliente**.

1. **Request HTTP**: El usuario (vía Postman o algún frontend) envía un POST a `/cuenta/{dni}`, con un JSON describiendo los datos de la cuenta (ej. tipo de cuenta, balance, moneda, etc.).
2. **Controller** (`CuentaController`):  
   - Recibe la petición y parsea el JSON a un objeto `Cuenta`.
   - Llama a `cuentaService.darDeAltaCuenta(cuenta, dni)`.
3. **Service** (`CuentaService`):  
   - Valida que la cuenta no sea `null`, que la moneda no sea `null`, que el saldo sea >= 0, etc.  
   - Busca al cliente titular usando `clienteService.buscarClientePorDni(dni)`.  
   - Chequea si el cliente ya tiene una cuenta del mismo tipo y misma moneda, y lanza excepción si corresponde.  
   - Llama a `clienteService.agregarCuenta(cuenta, dni)` (para vincular la cuenta con el cliente).  
   - Por último, llama a `cuentaDao.save(cuenta)` para persistir la nueva cuenta.
4. **DAO** (`CuentaDao`):  
   - Transforma el objeto `Cuenta` en un `CuentaEntity` y lo guarda en el `Map` de la base en memoria.  
   - Retorna el control a la capa de servicio.
5. **Service** y **Controller**:  
   - Devuelven una respuesta exitosa (ej. el objeto `Cuenta` creado o un mensaje JSON de confirmación).

El flujo es similar para **otras operaciones**:
- **Creación de cliente**: pasa por `ClienteController` -> `ClienteService` -> `ClienteDao`.
- **Transferencia**: pasa por `TransferController` -> `TransferService` -> `CuentaService` y `TransferDao`.  
   Se validan las cuentas, se actualizan saldos, se guarda la transferencia.

---

## 4. Cómo Testear los Endpoints

La aplicación está configurada para levantarse en `http://localhost:8080`. La siguiente lista describe los endpoints principales y cómo probarlos con un cliente HTTP (ej. **Postman**):

### 4.1 Cliente

1. **Dar de alta un cliente**  
   - **POST** `http://localhost:8080/cliente`  
   - **Body (JSON)**, ejemplo:
     ```json
     {
       "dni": 12345678,
       "nombre": "Juan",
       "apellido": "Pérez",
       "fechaNacimiento": "1990-01-01",
       "tipoPersona": "PERSONA_FISICA"
     }
     ```
   - Si todo va bien, retorna un 200 OK con el objeto creado o un mensaje de éxito.  
   - Si el cliente ya existe o es menor de 18, verás un 400 Bad Request con un mensaje de error.

2. **Buscar un cliente por DNI**  
   - **POST** `http://localhost:8080/cliente/{dni}`  
   - Ejemplo: `POST http://localhost:8080/cliente/12345678`  
   - Retorna un **Cliente** en JSON si existe o error si no existe.

3. **Borrar un cliente**  
   - **DELETE** `http://localhost:8080/cliente/{dni}`  
   - Ejemplo: `DELETE http://localhost:8080/cliente/12345678`  
   - Si todo va bien, retorna un 200 OK con un mensaje de éxito; o 400 si no existe el cliente.

---

### 4.2 Cuenta

1. **Crear una cuenta**  
   - **POST** `http://localhost:8080/cuenta/{dni}`  
   - **Body (JSON)**, ejemplo:
     ```json
     {
       "numeroCuenta": 1001,
       "balance": 50000,
       "tipoCuenta": "CAJA_AHORRO",
       "moneda": "PESOS"
     }
     ```
   - Espera un 200 OK con la cuenta creada o un 400 si hay validaciones que fallan (ej. cliente inexistente, cuenta duplicada, etc.).

2. **Agregar balance a una cuenta**  
   - **POST** `http://localhost:8080/cuenta/{id}/agregar-balance?monto={monto}`  
   - Ejemplo: `POST http://localhost:8080/cuenta/1001/agregar-balance?monto=10000`  
   - Retorna un mensaje confirmando el nuevo balance o error si la cuenta no existe o el monto es inválido.

3. **Consultar una cuenta por ID**  
   - **GET** `http://localhost:8080/cuenta/{id}`  
   - Ejemplo: `GET http://localhost:8080/cuenta/1001`  
   - Retorna la cuenta en JSON con su titular ya “hidratado” o un error si no existe.

4. **Obtener todas las cuentas de un cliente**  
   - **GET** `http://localhost:8080/cuenta/cliente/{dni}`  
   - Ejemplo: `GET http://localhost:8080/cuenta/cliente/12345678`  
   - Devuelve una lista de cuentas o un error si el cliente no existe o no tiene cuentas.

---

### 4.3 Transferencias

1. **Realizar una transferencia**  
   - Dependiendo de cómo hayas configurado, podría ser:
     - **POST** `http://localhost:8080/api/transfers`
   - **Body (JSON)**, ejemplo:
     ```json
     {
       "origen": 1001,
       "destino": 2002,
       "monto": 100000,
       "descripcion": "Pago de servicios",
       "moneda": "PESOS"
     }
     ```
   - Verifica que retorne 200 OK si la transferencia fue exitosa (teniendo saldo suficiente, cuentas existentes, misma moneda, etc.) o un 400 en caso de error.  
   - Si tu endpoint devuelves un “estado” en JSON (EXITOSA/FALLIDA), verás esa estructura en la respuesta.

2. **Obtener historial de movimientos**  
   - **GET** `http://localhost:8080/api/transfers/movements/{cvu}` (o la ruta que hayas definido)  
   - Ejemplo: `GET http://localhost:8080/api/transfers/movements/1001`  
   - Retorna todas las `TransferEntity` asociadas a la cuenta 1001 (ya sea como origen o destino).

---

## 5. Conclusiones

Este proyecto **ejemplifica** un sistema bancario básico en Java, con la siguiente arquitectura:
- **Controladores** que exponen endpoints REST con **Spring Boot**.
- **Servicios** que centralizan validaciones, reglas de negocio y coordinación de DAO.
- **DAOs** que se encargan del acceso a datos, usando una base en **memoria** para simplificar.
- **Modelo** de objetos (Cliente, Cuenta, Transfer) y **excepciones** personalizadas para mejorar la expresividad.

A futuro, se puede **migrar** a una persistencia real (MySQL, PostgreSQL, etc.) reemplazando los DAOs, sin alterar la lógica de la capa de servicio.
