package com.perroamor.inventory.config

import com.perroamor.inventory.entity.Product
import com.perroamor.inventory.entity.ProductVariant
import com.perroamor.inventory.entity.Event
import com.perroamor.inventory.entity.Sale
import com.perroamor.inventory.entity.SaleItem
import com.perroamor.inventory.service.ProductService
import com.perroamor.inventory.service.ProductVariantService
import com.perroamor.inventory.service.EventService
import com.perroamor.inventory.service.SaleService
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class DataInitializer(
    private val productService: ProductService,
    private val variantService: ProductVariantService,
    private val eventService: EventService,
    private val saleService: SaleService
) : CommandLineRunner {
    
    override fun run(vararg args: String?) {
        // Los productos ya se inicializan via Flyway migrations
        // Solo inicializar si no hay productos (fallback para desarrollo)
        if (productService.findAll().isEmpty()) {
            println("⚠️  No se encontraron productos. Flyway debería haberlos creado.")
            println("⚠️  Inicializando productos como fallback...")
            initializeProducts()
            initializeVariants()
        } else {
            println("✅ Productos ya inicializados via Flyway migrations")
        }
        
        // Eventos se inicializan solo para pruebas/desarrollo
        if (eventService.findAll().isEmpty()) {
            initializeEvents()
        }
        
        // Ventas de ejemplo solo para pruebas/desarrollo
        if (saleService.findAll().isEmpty()) {
            initializeSampleSales()
        }
    }
    
    private fun initializeProducts() {
        val products = listOf(
            Product(
                name = "Collar Santo Remedio",
                price = BigDecimal("199.00"),
                category = "Collares",
                brand = "Perro Amor",
                stock = 0, // Sin stock, usa variantes
                description = "Collar elegante con diseño único",
                canBePersonalized = true,
                hasVariants = true
            ),
            Product(
                name = "Correa Binomio",
                price = BigDecimal("189.00"),
                category = "Correas",
                brand = "Perro Amor",
                stock = 0, // Sin stock, usa variantes
                description = "Correa resistente con diseños únicos",
                canBePersonalized = false,
                hasVariants = true
            ),
            Product(
                name = "Porta Alerta",
                price = BigDecimal("139.00"),
                category = "Correas",
                brand = "Perro Amor",
                stock = 0, // Sin stock, usa variantes
                description = "Correa de seguridad con alta visibilidad",
                canBePersonalized = false,
                hasVariants = true
            ),
            Product(
                name = "Collar Vida Mía",
                price = BigDecimal("199.00"),
                category = "Collares",
                brand = "Perro Amor",
                stock = 10,
                description = "Collar con estilo único para tu mascota",
                canBePersonalized = true,
                hasVariants = false
            ),
            Product(
                name = "Mochila Mimi",
                price = BigDecimal("289.00"),
                category = "Mochilas",
                brand = "Perro Amor",
                stock = 5,
                description = "Mochila práctica para paseos largos",
                canBePersonalized = false,
                hasVariants = false
            ),
            Product(
                name = "Grabado de Nombre",
                price = BigDecimal("50.00"),
                category = "Personalización",
                brand = "Perro Amor",
                stock = 100,
                description = "Servicio de grabado de nombre en productos",
                canBePersonalized = false,
                hasVariants = false
            ),
            Product(
                name = "Grabado de Teléfono",
                price = BigDecimal("50.00"),
                category = "Personalización",
                brand = "Perro Amor",
                stock = 100,
                description = "Servicio de grabado de teléfono en productos",
                canBePersonalized = false,
                hasVariants = false
            )
        )
        
        products.forEach { productService.save(it) }
        println("Datos de prueba inicializados: ${products.size} productos agregados")
    }
    
    private fun initializeVariants() {
        val products = productService.findAll()
        println("Productos encontrados para variantes: ${products.size}")
        products.forEach { println("- ${it.id}: ${it.name}") }
        
        // Variantes para Collar Santo Remedio
        val collarSantoRemedio = products.find { it.name == "Collar Santo Remedio" }
        collarSantoRemedio?.let { product ->
            println("Creando variantes para Collar Santo Remedio (ID: ${product.id})")
            val variants = listOf(
                ProductVariant(
                    product = product,
                    variantName = "Listón Rojo - S",
                    color = "Rojo",
                    size = "S",
                    material = "Listón 1.9cm",
                    stock = 15,
                    sku = "CSR-R-S",
                    shopifyVariantId = "gid://shopify/ProductVariant/44123456789",
                    shopifyProductId = "gid://shopify/Product/8123456789"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Listón Azul - M",
                    color = "Azul",
                    size = "M",
                    material = "Listón 3cm",
                    stock = 12,
                    sku = "CSR-A-M",
                    shopifyVariantId = "gid://shopify/ProductVariant/44123456790",
                    shopifyProductId = "gid://shopify/Product/8123456789"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Listón Verde - L",
                    color = "Verde",
                    size = "L",
                    material = "Listón 3cm",
                    stock = 8,
                    sku = "CSR-V-L",
                    shopifyVariantId = "gid://shopify/ProductVariant/44123456791",
                    shopifyProductId = "gid://shopify/Product/8123456789"
                )
            )
            variants.forEach { 
                val saved = variantService.save(it)
                println("Variante guardada: ${saved.id} - ${saved.variantName}")
            }
        }
        
        // Variantes para Correa Binomio
        val correaBinomio = products.find { it.name == "Correa Binomio" }
        correaBinomio?.let { product ->
            val variants = listOf(
                ProductVariant(
                    product = product,
                    variantName = "Pilatos Dog - M",
                    design = "Pilatos Dog",
                    color = "Multicolor",
                    size = "M",
                    stock = 8,
                    sku = "CB-PD-M",
                    shopifyVariantId = "gid://shopify/ProductVariant/44987654321",
                    shopifyProductId = "gid://shopify/Product/8987654321"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Dolce Vida - L",
                    design = "Dolce Vida",
                    color = "Rosa/Dorado",
                    size = "L",
                    stock = 6,
                    sku = "CB-DV-L",
                    shopifyVariantId = "gid://shopify/ProductVariant/44987654322",
                    shopifyProductId = "gid://shopify/Product/8987654321"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Ohana - S",
                    design = "Ohana",
                    color = "Azul/Blanco",
                    size = "S",
                    stock = 4,
                    sku = "CB-OH-S"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Love is Love - Único",
                    design = "Love is Love",
                    color = "Arcoíris",
                    size = "Único",
                    stock = 3,
                    sku = "CB-LIL-U"
                )
            )
            variants.forEach { variantService.save(it) }
        }
        
        // Variantes para Porta Alerta
        val portaAlerta = products.find { it.name == "Porta Alerta" }
        portaAlerta?.let { product ->
            val variants = listOf(
                ProductVariant(
                    product = product,
                    variantName = "Rojo - S",
                    color = "Rojo",
                    size = "S",
                    stock = 10,
                    sku = "PA-R-S"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Morado - M",
                    color = "Morado",
                    size = "M",
                    stock = 8,
                    sku = "PA-M-M"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Rosa - L",
                    color = "Rosa",
                    size = "L",
                    stock = 12,
                    sku = "PA-RS-L"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Naranja - Mediano",
                    color = "Naranja",
                    size = "Mediano",
                    stock = 6,
                    sku = "PA-N-MD"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Amarillo - Grande",
                    color = "Amarillo",
                    size = "Grande",
                    stock = 9,
                    sku = "PA-A-G"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Verde - Unitalla",
                    color = "Verde",
                    size = "Unitalla",
                    stock = 7,
                    sku = "PA-V-U"
                ),
                ProductVariant(
                    product = product,
                    variantName = "Azul - XL",
                    color = "Azul",
                    size = "XL",
                    stock = 11,
                    sku = "PA-AZ-XL"
                )
            )
            variants.forEach { variantService.save(it) }
        }
        
        println("Variantes de productos inicializadas")
    }
    
    private fun initializeEvents() {
        val today = LocalDate.now()
        val events = listOf(
            Event(
                name = "Expo Mascotas CDMX 2024",
                startDate = today.minusDays(3),
                endDate = today.plusDays(4),
                location = "Centro Banamex, Ciudad de México",
                description = "La feria de mascotas más grande de México",
                isActive = true
            ),
            Event(
                name = "Festival Pet Friendly Polanco",
                startDate = today.plusDays(10),
                endDate = today.plusDays(12),
                location = "Parque Lincoln, Polanco",
                description = "Festival familiar con productos para mascotas",
                isActive = true
            ),
            Event(
                name = "Mercado de Pulgas Pet Coyoacán",
                startDate = today.minusDays(20),
                endDate = today.minusDays(18),
                location = "Plaza Coyoacán",
                description = "Mercado mensual de productos para mascotas",
                isActive = true
            ),
            Event(
                name = "ExpoVet Guadalajara 2024",
                startDate = today.plusDays(25),
                endDate = today.plusDays(27),
                location = "Expo Guadalajara",
                description = "Exposición veterinaria y de productos para mascotas",
                isActive = true
            ),
            Event(
                name = "Feria de Adopción Santa Fe",
                startDate = today.plusDays(2),
                endDate = today.plusDays(2),
                location = "Centro Comercial Santa Fe",
                description = "Feria de adopción con venta de accesorios",
                isActive = true
            )
        )
        
        events.forEach { eventService.save(it) }
        println("Datos de eventos inicializados: ${events.size} eventos agregados")
    }
    
    private fun initializeSampleSales() {
        val events = eventService.findAll()
        val products = productService.findAll()
        
        if (events.isEmpty() || products.isEmpty()) {
            println("No hay eventos o productos para crear ventas de ejemplo")
            return
        }
        
        // Buscar el evento en curso (Expo Mascotas CDMX 2024)
        val currentEvent = events.find { it.name.contains("Expo Mascotas CDMX") }
        if (currentEvent == null) {
            println("No se encontró evento en curso para crear ventas de ejemplo")
            return
        }
        
        val today = LocalDate.now()
        val baseDateTime = today.atTime(10, 0) // Empezar a las 10:00 AM
        
        // Crear ventas de ejemplo con diferentes métodos de pago
        val sampleSales = listOf(
            // Venta 1 - Efectivo
            Sale(
                event = currentEvent,
                saleDate = baseDateTime.plusHours(1),
                customerName = "María González",
                customerPhone = "555-1234",
                paymentMethod = "Efectivo",
                totalAmount = BigDecimal("398.00")
            ),
            // Venta 2 - Tarjeta de Crédito
            Sale(
                event = currentEvent,
                saleDate = baseDateTime.plusHours(2),
                customerName = "Carlos Pérez",
                paymentMethod = "Tarjeta de Crédito",
                totalAmount = BigDecimal("189.00")
            ),
            // Venta 3 - Efectivo
            Sale(
                event = currentEvent,
                saleDate = baseDateTime.plusHours(3),
                paymentMethod = "Efectivo",
                totalAmount = BigDecimal("549.00")
            ),
            // Venta 4 - Transferencia
            Sale(
                event = currentEvent,
                saleDate = baseDateTime.plusHours(4),
                customerName = "Ana Rodríguez",
                customerPhone = "555-5678",
                paymentMethod = "Transferencia",
                totalAmount = BigDecimal("289.00")
            ),
            // Venta 5 - Tarjeta de Débito
            Sale(
                event = currentEvent,
                saleDate = baseDateTime.plusHours(5),
                customerName = "Luis Martín",
                paymentMethod = "Tarjeta de Débito",
                totalAmount = BigDecimal("139.00")
            ),
            // Venta 6 - Efectivo
            Sale(
                event = currentEvent,
                saleDate = baseDateTime.plusHours(6),
                paymentMethod = "Efectivo",
                totalAmount = BigDecimal("199.00")
            )
        )
        
        sampleSales.forEach { sale ->
            val savedSale = saleService.save(sale)
            println("Venta de ejemplo creada: #${savedSale.id} - ${savedSale.paymentMethod} - $${savedSale.totalAmount}")
        }
        
        println("Ventas de ejemplo inicializadas: ${sampleSales.size} ventas creadas para ${currentEvent.name}")
    }
}