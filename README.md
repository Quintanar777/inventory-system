# 🐕 Sistema de Inventario - Perro Amor

Sistema de gestión de inventario y ventas para eventos de Perro Amor, desarrollado con Spring Boot, Kotlin y Vaadin.

## 🚀 Características Principales

### 📦 Gestión de Productos
- **Productos multi-marca**: Perro Amor y Perra Madre
- **Sistema de variantes**: Colores, tallas, diseños, materiales
- **Control de stock** por producto y variante
- **Categorización** (Collares, Correas, Mochilas, Premios, etc.)
- **Personalización** (grabado de nombres y teléfonos)

### 🎪 Gestión de Eventos
- **Eventos con fechas**: Control de inicio y fin
- **Estados automáticos**: En curso, próximos, finalizados
- **Ubicación y descripción** detallada
- **Navegación directa** a ventas por evento

### 💰 Sistema de Ventas
- **Punto de venta completo** para eventos presenciales
- **Múltiples productos por venta** con cantidades variables
- **Edición de precios** durante la venta
- **Filtrado por marca** con radio buttons
- **Métodos de pago**: Efectivo, tarjetas, transferencia
- **Cálculo automático** de totales

### 📊 Estadísticas y Reportes
- **Estadísticas por evento**: Total de ventas y ingresos
- **Desglose por método de pago** con iconos visuales
- **Detalles de venta**: Productos, cantidades, personalizaciones
- **Estado de ventas**: Pagadas, pendientes, canceladas

## 🛠 Stack Tecnológico

- **Backend**: Spring Boot 3.5.4 + Kotlin 1.9.25
- **Frontend**: Vaadin 24.8.6 (UI Components)
- **Base de Datos**: H2 (desarrollo) / PostgreSQL (producción)
- **Migraciones**: Flyway para versionado de BD
- **Build**: Gradle 8.14.3

## 📁 Estructura del Proyecto

```
src/main/kotlin/com/perroamor/inventory/
├── entity/           # Entidades JPA (Product, Sale, Event, etc.)
├── repository/       # Repositorios JPA con consultas custom
├── service/          # Lógica de negocio y servicios
├── view/            # Vistas Vaadin y componentes UI
│   └── component/   # Componentes reutilizables (SaleItemDialog)
└── config/          # Configuración y DataInitializer

src/main/resources/
├── db/migration/    # Migraciones Flyway (V1, V2, V3, V4)
└── application.properties
```

## 🗄 Base de Datos

### Migraciones Flyway
- **V1**: Esquema inicial (tablas principales)
- **V2**: Productos iniciales de Perro Amor + variantes
- **V3**: Campo "brand" para multi-marca
- **V4**: Productos de ejemplo de Perra Madre

### Entidades Principales
- `Product` → `ProductVariant` (1:N)
- `Event` → `Sale` → `SaleItem` (1:N:N)
- `SaleItem` → `Product` + `ProductVariant` (M:1)

## 🚦 Instalación y Uso

### Prerrequisitos
- Java 21+
- Gradle 8+

### Ejecución
```bash
# Clonar repositorio
git clone [repository-url]
cd inventory-system

# Ejecutar aplicación
./gradlew bootRun

# Acceder a la aplicación
open http://localhost:8080
```

### Consola H2 (desarrollo)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuario: `sa`
- Password: `password`

## 🎯 Flujos de Usuario

### 1. Gestión de Eventos
1. Crear evento con fechas y ubicación
2. Ver estado automático (en curso/próximo/finalizado)
3. Navegar a ventas desde botones del evento

### 2. Nueva Venta (Punto de Venta)
1. Se auto-selecciona evento en curso
2. **Filtrar por marca** (Perro Amor / Perra Madre)
3. Seleccionar productos de la marca elegida
4. Agregar variantes, cantidades y personalización
5. Calcular total automáticamente
6. Procesar pago y guardar

### 3. Gestión de Productos
1. Ver tabla completa con marcas
2. Crear/editar productos con ComboBox de marcas
3. Navegar a gestión de variantes
4. Control de stock por producto/variante

## 📈 Datos de Ejemplo

### Productos Perro Amor (7)
- Collar Santo Remedio ($199) - 3 variantes
- Correa Binomio ($189) - 4 diseños
- Porta Alerta ($139) - 7 colores/tallas  
- Collar Vida Mía ($199)
- Mochila Mimi ($289)
- Servicios de grabado ($50 c/u)

### Productos Perra Madre (3)
- Premio Natural Pollo ($85)
- Snack Dental ($65)
- Huesos de Cuero Natural ($45) - 3 tamaños

## 🔧 Configuración

### Perfiles de Entorno
- **Desarrollo**: H2 en memoria + datos de ejemplo
- **Producción**: PostgreSQL (configurar en application-prod.properties)

### Variables de Entorno
```properties
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost/inventory
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password

# Flyway
SPRING_FLYWAY_ENABLED=true
```

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto es privado y pertenece a Perro Amor.

---

**Desarrollado con ❤️ para Perro Amor** 🐕