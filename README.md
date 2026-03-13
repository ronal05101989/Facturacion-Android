# Facturacion Android

Proyecto base para una aplicacion Android de facturacion construida con Kotlin y Jetpack Compose.

## Alcance inicial

- Pantalla de resumen con metricas clave.
- Modulo visual de clientes.
- Modulo visual de productos.
- Modulo visual de facturas con filtro de pendientes.
- Datos de ejemplo para validar el flujo de navegacion.

## Estructura

- `app/src/main/java/com/playground/facturacion/MainActivity.kt`: punto de entrada.
- `app/src/main/java/com/playground/facturacion/ui/FacturacionApp.kt`: navegacion y pantallas principales.
- `app/src/main/java/com/playground/facturacion/ui/model`: modelos de dominio.
- `app/src/main/java/com/playground/facturacion/ui/data/SampleData.kt`: datos semilla.
- `gradlew` y `gradlew.bat`: wrapper de Gradle para abrir y compilar el proyecto.

## Siguientes pasos sugeridos

1. Persistencia local con Room para clientes, productos y facturas.
2. Formularios para crear y editar registros.
3. Calculo de impuestos, descuentos y numeracion de facturas.
4. Exportacion a PDF y envio por WhatsApp o correo.
5. Sincronizacion con backend o API fiscal si aplica a tu pais.

## Para ejecutarlo

1. Instala Android Studio.
2. Instala un SDK Android reciente desde el SDK Manager.
3. Abre esta carpeta como proyecto.
4. Si hace falta, crea `local.properties` a partir de `local.properties.example`.
5. Ejecuta la app en un emulador o dispositivo fisico.

Nota: todavia falta `gradle/wrapper/gradle-wrapper.jar`, que Android Studio o una instalacion local de Gradle puede regenerar al sincronizar el proyecto.
