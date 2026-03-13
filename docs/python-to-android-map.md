# Mapa del sistema Python a Android

Archivo de referencia analizado:
`C:\Users\Core-i7-Vpro\Documents\programas\vende_facil_rd_farmacia_vencimiento_autofecha.py`

## Modulos detectados en el sistema Python

- Activacion de licencia.
- Inicio de sesion por usuario.
- POS principal de ventas.
- Inventario de productos.
- Alertas de stock bajo.
- Manejo de vencimiento de productos.
- Historial y edicion de facturas.
- Reporte diario.
- Gestion de usuarios.
- Configuracion de factura e impresora.
- Backup automatico.

## Traduccion propuesta a Android

- `POS`: pantalla principal para venta rapida, carrito, total, devuelta e impresion.
- `Stock`: productos, alertas de inventario y vencimientos.
- `Facturas`: historial, detalle, edicion y eliminacion.
- `Reportes`: ventas del dia, ganancia, metodos de pago y productos mas vendidos.
- `Config`: licencia, usuarios, negocio, impresora y backup.

## Equivalencias de UI

- Barra superior de botones del sistema Tkinter:
  se convierte en accesos por secciones y tarjetas de accion.
- Tabla de productos:
  se convierte en lista de catalogo con busqueda y filtros.
- Carrito lateral:
  se convierte en una vista de factura actual dentro del flujo POS.
- Ventanas emergentes:
  se convierten en pantallas de detalle o dialogos moviles.
- Restriccion por rol:
  se mantiene con visibilidad condicional de modulos.

## Prioridad recomendada para la app Android

1. Login y roles.
2. POS y carrito de venta.
3. Inventario con vencimientos.
4. Historial de facturas.
5. Reporte diario.
6. Configuracion y backup.
