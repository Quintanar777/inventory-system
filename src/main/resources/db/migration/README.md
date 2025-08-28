# Flyway Database Migrations

Este directorio contiene las migraciones de base de datos para el sistema de inventario de Perro Amor.

## Estructura de Migraciones

### V1__Initial_schema.sql
- **Propósito**: Crea el esquema inicial de la base de datos
- **Contenido**: 
  - Tablas: `events`, `products`, `product_variants`, `sales`, `sale_items`
  - Índices para optimizar consultas
  - Restricciones de clave foránea

### V2__Initial_products.sql
- **Propósito**: Inserta los productos iniciales del catálogo
- **Contenido**:
  - 7 productos base (collares, correas, mochilas, servicios de grabado)
  - Variantes de productos con stock específico
  - Información completa de SKU, colores, tallas, materiales

## Convenciones de Nomenclatura

```
V{version}__{descripcion}.sql

Ejemplos:
- V1__Initial_schema.sql
- V2__Initial_products.sql  
- V3__Add_customer_table.sql
- V4__Update_product_prices.sql
```

## Configuración

Las migraciones están configuradas en `application.properties`:

```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.validate-on-migrate=true
```

## Comandos Útiles

### Ver estado de migraciones
```bash
./gradlew flywayInfo
```

### Validar migraciones
```bash
./gradleway flywayValidate
```

### Limpiar base de datos (⚠️ CUIDADO en producción)
```bash
./gradlew flywayClean
```

## Integración con DataInitializer

El `DataInitializer.kt` ahora:
- ✅ **NO** inicializa productos (los maneja Flyway)
- ✅ **SÍ** inicializa eventos de ejemplo para desarrollo
- ✅ **SÍ** inicializa ventas de ejemplo para pruebas

## Productos Incluidos en V2

1. **Collar Santo Remedio** ($199) - Con variantes de listón
2. **Correa Binomio** ($189) - Con diseños únicos
3. **Porta Alerta** ($139) - Correa de seguridad con múltiples colores
4. **Collar Vida Mía** ($199) - Sin variantes, stock directo
5. **Mochila Mimi** ($289) - Sin variantes
6. **Grabado de Nombre** ($50) - Servicio de personalización
7. **Grabado de Teléfono** ($50) - Servicio de personalización

Total: **18 variantes de producto** distribuidas en los productos principales.

## Ventajas de usar Flyway

✅ **Control de versión** de la base de datos
✅ **Migraciones automáticas** en cada deploy
✅ **Rollback seguro** si es necesario
✅ **Datos consistentes** entre entornos
✅ **Trazabilidad** de cambios en la DB