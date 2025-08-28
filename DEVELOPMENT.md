# 🛠 Guía de Desarrollo - Sistema de Inventario

Esta guía contiene información técnica para desarrolladores que trabajen en el sistema.

## 📋 Estado del Proyecto

### ✅ Funcionalidades Implementadas

#### Core Sistema
- [x] Entidades JPA completas (Product, Sale, Event, etc.)
- [x] Repositorios con consultas custom
- [x] Servicios con lógica de negocio
- [x] Migraciones Flyway (V1-V4)
- [x] Configuración Spring Boot + Vaadin

#### Gestión de Productos
- [x] CRUD completo de productos
- [x] Sistema multi-marca (Perro Amor / Perra Madre)
- [x] Variantes con stock independiente
- [x] Categorización y personalización
- [x] Vista de productos con filtros

#### Gestión de Eventos
- [x] CRUD de eventos con fechas
- [x] Estados automáticos (en curso/próximo/finalizado)
- [x] Navegación a ventas desde eventos
- [x] Vista optimizada con botones de acción

#### Sistema de Ventas
- [x] Punto de venta completo (NewSaleView)
- [x] Filtrado por marca con RadioButtonGroup
- [x] Selección de productos/variantes con stock
- [x] Edición de precios durante venta
- [x] Cálculo automático de totales
- [x] Vista de ventas con estadísticas

## 🏗 Arquitectura del Código

### Patrón MVC + Repository
```
Controller (Vaadin Views)
    ↓
Service (Business Logic)
    ↓
Repository (Data Access)
    ↓
Entity (JPA/Hibernate)
```

### Estructura por Capas
- **View**: Interfaces Vaadin (ProductView, SaleView, etc.)
- **Service**: Lógica de negocio (ProductService, SaleService, etc.)  
- **Repository**: Acceso a datos JPA
- **Entity**: Modelos de dominio con anotaciones JPA
- **Config**: Configuración y inicialización de datos

## 🗄 Base de Datos

### Esquema Principal
```sql
products (id, name, brand, category, price, stock, ...)
    ↓
product_variants (id, product_id, variant_name, color, size, stock, ...)

events (id, name, location, start_date, end_date, is_active)
    ↓  
sales (id, event_id, sale_date, payment_method, total_amount, ...)
    ↓
sale_items (id, sale_id, product_id, variant_id, quantity, unit_price, ...)
```

### Flyway Migrations
- **V1**: Esquema inicial con todas las tablas
- **V2**: Productos base + variantes de Perro Amor  
- **V3**: Campo "brand" para soporte multi-marca
- **V4**: Productos de ejemplo de Perra Madre

## 🎨 Frontend (Vaadin)

### Componentes Principales
- **ProductView**: Grid + formulario de productos
- **EventView**: Grid de eventos + navegación
- **NewSaleView**: Punto de venta completo
- **SaleView**: Lista de ventas + estadísticas
- **SaleItemDialog**: Selección de productos para venta

### Patrones UI
- **Grid + Form**: Para listados con edición
- **Dialog**: Para formularios modales
- **ComboBox**: Para selecciones con filtrado
- **RadioButtonGroup**: Para opciones excluyentes
- **Layout responsivo**: HorizontalLayout + VerticalLayout

## ⚙️ Configuración de Entorno

### Desarrollo Local
```properties
# application.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

### Producción (Ejemplo)
```properties  
# application-prod.properties
spring.datasource.url=jdbc:postgresql://localhost/inventory_prod
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.flyway.baseline-on-migrate=false
```

## 🧪 Testing

### Estructura de Tests
```kotlin
@SpringBootTest
class ProductServiceTest {
    @Autowired
    lateinit var productService: ProductService
    
    @Test
    fun `should find products by brand`() {
        // Test implementation
    }
}
```

### Datos de Prueba
- **DataInitializer**: Datos automáticos en desarrollo
- **Test fixtures**: Para tests unitarios
- **H2 Database**: Base en memoria para tests

## 🔄 Flujos de Trabajo

### Agregar Nueva Funcionalidad

1. **Entidad** (si es necesaria)
   ```kotlin
   @Entity
   @Table(name = "nueva_entidad")
   data class NuevaEntidad(...)
   ```

2. **Migración Flyway**
   ```sql
   -- V5__Add_nueva_funcionalidad.sql
   CREATE TABLE nueva_entidad (...);
   ```

3. **Repository**
   ```kotlin
   interface NuevaRepository : JpaRepository<NuevaEntidad, Long> {
       fun findByCustomField(field: String): List<NuevaEntidad>
   }
   ```

4. **Service**  
   ```kotlin
   @Service
   class NuevaService(private val repository: NuevaRepository) {
       fun businessLogic(): List<NuevaEntidad> = repository.findAll()
   }
   ```

5. **View**
   ```kotlin
   @Route("nueva", layout = MainLayout::class)
   class NuevaView(@Autowired private val service: NuevaService) : VerticalLayout()
   ```

### Proceso de Commits
```bash
# Feature branch
git checkout -b feature/nueva-funcionalidad

# Desarrollo + commits
git add .
git commit -m "feat: nueva funcionalidad con descripción detallada"

# Push y PR
git push origin feature/nueva-funcionalidad
```

## 🚀 Despliegue

### Build de Producción
```bash
# Compilar con perfil de producción
./gradlew build -Pvaadin.productionMode=true

# Generar JAR ejecutable  
./gradlew bootJar

# Ejecutar en servidor
java -jar build/libs/inventory-system-*.jar --spring.profiles.active=prod
```

### Docker (Futuro)
```dockerfile
FROM openjdk:21-jre-slim
COPY build/libs/inventory-system-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 📚 Referencias Técnicas

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Vaadin Documentation](https://vaadin.com/docs)
- [Kotlin for Spring](https://kotlinlang.org/docs/spring-boot-restful.html)
- [Flyway Migration Guide](https://flywaydb.org/documentation/)

---

**Happy Coding! 🐕💻**