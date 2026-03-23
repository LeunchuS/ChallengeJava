# Challenge Java - Procesamiento de Transacciones

Proyecto desarrollado en Java 17 con Spring Boot para la carga y consulta de transacciones a partir de archivos CSV.

Permite:
- Procesar archivos CSV con transacciones
- Consultar procesos de carga
- Consultar balances por cuenta
- Obtener ranking de cuentas
- Validar transacciones duplicadas

## Requisitos

- Java 17
- Gradle

## Ejecución

1. Clonar el repositorio:
   git clone https://github.com/LeunchuS/ChallengeJava.git

2. Ingresar al directorio del proyecto:
   cd ChallengeJava

3. Ejecutar:
   ./gradlew bootRun

## Acceso

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 Database console: http://localhost:8080/h2-console

## Dataset

El sistema permite cargar archivos CSV con el siguiente formato:

transactionId,accountId,amount,type,timestamp

Ejemplo:

```csv
0,528,8197.03,DEBIT,2026-03-18T10:00:00
1,995,7902.0,DEBIT,2026-03-18T10:00:01
2,678,5163.81,DEBIT,2026-03-18T10:00:02
``` 

Se incluyen dos archivos de prueba en la raiz del proyecto: csvTransaction395k.csv y csvTransactionEmpty.csv


## Endpoint de carga de CSV

- POST /fileProcess/upload

Permite cargar un archivo CSV para su procesamiento asincrónico.

Para utilizarlo:
1. Abrir la url de swagger provista
2. Dirigirse a la sección **File Processing**
3. Seleccionar el endpoint `/fileProcess/upload`
4. Hacer click en **Try it out**, habilitando así el botón de carga.
5. Cargar uno de los archivos provistos de muestra.
6. Ejecutar la request con el boton azul **Execute**
7. Verificar resultado en **Responses**

## Otros endpoints

Solo para consultas

- GET /consulting/processing/{processingId}
- GET /consulting/accountBalance/{accountId}
- GET /consulting/ranking
- GET /consulting/duplicated/{transactionId}

Estos endpoints pueden probarse directamente desde Swagger UI ingresando los parámetros correspondientes.


## Decisiones de diseño

- Se utilizó una arquitectura en capas (Controller, Service, Repository).  para dar orden al código, separar responsabilidades y permitir cambiar la implementación de cada capa sin afectar al resto.
- Se implementó un manejador global de excepciones con @RestControllerAdvice para facilitar mantenimiento
- Se utiliza BigDecimal para manejar montos y evitar errores de precisión.
- Se implementó el patrón Producer-Consumer con hilos para acelerar el procesamiento. El producto se encarga de generar 
una cola de strings por cada linea del csv leida. Varios consumidores leen la cola y buscan convertir las lineas en transacciones para luego persistirlas en base.  
- Se documentaron los endpoints con OpenAPI (Swagger). Desde donde, además, pueden probarse.
- Se centralizó la gestión de errores mediante una anotación custom (@DefaultApiResponses) priorizando los códigos HTTP:
   - 400: errores de validación
   - 404: recursos no encontrados
   - 500: errores inesperados
- Se utilizó H2 como base de datos en memoria para facilitar pruebas dado que corre en memoria, arranca con la app y
  se limpia sola al detener la ejecución.