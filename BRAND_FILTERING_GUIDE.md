# 🏷️ Funcionalidad de Filtrado por Marca

Esta funcionalidad permite filtrar productos por marca en el diálogo de "Agregar Producto a la Venta", facilitando la selección de productos específicos durante el proceso de ventas.

## ✨ Características Implementadas

### 🔘 RadioButtonGroup para Filtrar por Marca
- **Ubicación**: Diálogo "Agregar Producto a la Venta"
- **Opciones disponibles**: 
  - 🐕 "Perro Amor" (marca principal)
  - 🦴 "Perra Madre" (premios y snacks)
- **Selección por defecto**: "Perro Amor"

### 🔄 Filtrado Dinámico
- **Funcionamiento**: Al seleccionar una marca, se cargan solo los productos de esa marca
- **Limpieza automática**: Al cambiar la marca se limpian las selecciones previas de producto y variante
- **Actualización de precios**: Se resetea el precio unitario al cambiar de marca

### 📋 Productos por Marca

#### Perro Amor (7 productos):
- Collar Santo Remedio ($199) - 3 variantes de listón
- Correa Binomio ($189) - 4 variantes con diseños
- Porta Alerta ($139) - 7 variantes de colores/tallas
- Collar Vida Mía ($199) - Sin variantes
- Mochila Mimi ($289) - Sin variantes
- Grabado de Nombre ($50) - Servicio de personalización
- Grabado de Teléfono ($50) - Servicio de personalización

#### Perra Madre (3 productos):
- Premio Natural Pollo ($85) - Premios para entrenamiento
- Snack Dental ($65) - Higiene bucal
- Huesos de Cuero Natural ($45) - 3 variantes de tamaños

## 🎯 Flujo de Usuario

1. **Abrir Nueva Venta**: Navegar a "Nueva Venta" desde el menú
2. **Agregar Producto**: Hacer clic en "Agregar Producto" 
3. **Seleccionar Marca**: Usar los radio buttons para elegir "Perro Amor" o "Perra Madre"
4. **Elegir Producto**: El ComboBox solo muestra productos de la marca seleccionada
5. **Completar Venta**: Continuar con cantidad, precio y personalización

## 🔧 Implementación Técnica

### Base de Datos
- **Migración V3**: Agrega campo `brand` a la tabla `products`
- **Migración V4**: Inserta productos de ejemplo de "Perra Madre"
- **Repositorio**: `ProductRepository.findByBrand(brand: String)`
- **Servicio**: `ProductService.findByBrand(brand: String)`

### Frontend
- **Componente**: `SaleItemDialog.kt`
- **Control**: `RadioButtonGroup<String>`
- **Filtrado**: Método `loadProductsByBrand(brand: String)`
- **Eventos**: Listener que limpia selecciones al cambiar marca

### Flujo de Datos
```
Usuario selecciona marca 
    ↓
RadioButtonGroup.addValueChangeListener
    ↓
loadProductsByBrand(selectedBrand)
    ↓
ProductService.findByBrand(brand)
    ↓
ProductRepository.findByBrand(brand)
    ↓
Actualizar ComboBox de productos
    ↓
Limpiar selecciones previas
```

## 🎨 Interfaz de Usuario

### Diálogo Actualizado
```
┌─────────────────────────────────────┐
│         Agregar Producto a la Venta │
├─────────────────────────────────────┤
│ Filtrar por Marca:                  │
│ ○ Perro Amor  ● Perra Madre         │
├─────────────────────────────────────┤
│ Producto: [ComboBox filtrado]       │
│ Variante: [ComboBox]                │
│ Cantidad: [1]                       │
│ Precio Unitario: [Campo editable]   │
│ Personalización: [Campo opcional]   │
├─────────────────────────────────────┤
│                    Total: $0.00     │
├─────────────────────────────────────┤
│  [Agregar a Venta]    [Cancelar]    │
└─────────────────────────────────────┘
```

### Etiquetas de Productos
- **Formato**: `"Nombre - Marca (Categoría) - $Precio"`
- **Ejemplo**: `"Premio Natural Pollo - Perra Madre (Premios) - $85.00"`

## ✅ Beneficios

1. **🎯 Búsqueda más eficiente**: Menos productos para revisar
2. **🏷️ Identificación clara**: Fácil distinción entre marcas
3. **⚡ Carga optimizada**: Solo productos relevantes
4. **🎨 Interfaz limpia**: RadioButtons intuitivos
5. **🔄 Flujo mejorado**: Limpieza automática de selecciones

## 🚀 Próximas Mejoras

- [ ] Agregar más marcas según catálogo
- [ ] Filtrado por categoría + marca
- [ ] Búsqueda de texto dentro de marca seleccionada
- [ ] Estadísticas de ventas por marca
- [ ] Colores distintivos por marca