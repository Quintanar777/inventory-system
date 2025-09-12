package com.perroamor.inventory.view.component

import com.perroamor.inventory.entity.Product
import com.perroamor.inventory.entity.Brand
import com.perroamor.inventory.service.ProductService
import com.perroamor.inventory.service.BrandService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.BigDecimalField
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.NumberField
import java.math.BigDecimal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ProductSearchMobile(
    @Autowired private val productService: ProductService,
    @Autowired private val brandService: BrandService
) {
    
    private lateinit var dialog: Dialog
    private lateinit var contentArea: VerticalLayout
    private lateinit var brandButtonsLayout: HorizontalLayout
    private lateinit var productsGridArea: Div
    private lateinit var selectedBrandLabel: Span
    private lateinit var addToSaleButton: Button
    
    private var brands = listOf<Brand>()
    private var currentSelectedBrand: Brand? = null
    private var isWholesaleMode: Boolean = false
    private val selectedProducts = mutableMapOf<Long, ProductSelectionData>()
    private val productCounterElements = mutableMapOf<Long, Span>()
    data class ProductSelectionData(
        val product: Product,
        val quantity: Int,
        val unitPrice: BigDecimal
    )
    
    private var onProductSelectCallback: ((ProductSelectionData) -> Unit)? = null
    
    fun show(isWholesaleMode: Boolean = false, onProductSelect: (ProductSelectionData) -> Unit) {
        this.onProductSelectCallback = onProductSelect
        this.isWholesaleMode = isWholesaleMode
        selectedProducts.clear()
        productCounterElements.clear()
        
        // Resetear estado inicial
        currentSelectedBrand = null
        
        loadBrands() // Cargar marcas desde la base de datos
        createDialog()
        setupComponents()
        dialog.open()
    }
    
    private fun createDialog() {
        dialog = Dialog()
        dialog.isCloseOnEsc = true
        dialog.isCloseOnOutsideClick = false
        dialog.width = "90vw"
        dialog.height = "80vh"
        dialog.element.style.set("max-width", "800px")
        
        // Área principal de contenido
        contentArea = VerticalLayout()
        contentArea.setSizeFull()
        contentArea.isPadding = true
        contentArea.isSpacing = true
        
        // Encabezado del diálogo
        val headerLayout = createHeader()
        
        // Área de botones de marca
        brandButtonsLayout = createBrandButtons()
        
        // Label para mostrar la marca seleccionada
        selectedBrandLabel = Span("Selecciona una marca para ver los productos")
        selectedBrandLabel.element.style.set("font-weight", "bold")
        selectedBrandLabel.element.style.set("font-size", "1.2em")
        selectedBrandLabel.element.style.set("color", "var(--lumo-primary-text-color)")
        selectedBrandLabel.element.style.set("text-align", "center")
        selectedBrandLabel.element.style.set("margin", "16px 0")
        
        // Área del grid de productos
        productsGridArea = Div()
        productsGridArea.setWidthFull()
        productsGridArea.element.style.set("overflow", "auto")
        productsGridArea.element.style.set("flex", "1")
        
        // Botón flotante "Agregar a Venta" (inicialmente oculto)
        addToSaleButton = createFloatingAddToSaleButton()
        
        contentArea.add(
            headerLayout,
            brandButtonsLayout,
            selectedBrandLabel,
            productsGridArea,
            addToSaleButton
        )
        
        // Configurar flex grow
        contentArea.setFlexGrow(0.0, headerLayout)
        contentArea.setFlexGrow(0.0, brandButtonsLayout)
        contentArea.setFlexGrow(0.0, selectedBrandLabel)
        contentArea.setFlexGrow(1.0, productsGridArea)
        contentArea.setFlexGrow(0.0, addToSaleButton)
        
        dialog.add(contentArea)
    }
    
    private fun createHeader(): VerticalLayout {
        val headerContainer = VerticalLayout()
        headerContainer.isSpacing = false
        headerContainer.isPadding = false
        
        // Fila superior con título y botón cerrar
        val topRow = HorizontalLayout()
        topRow.setWidthFull()
        topRow.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        topRow.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
        
        val modeText = if (isWholesaleMode) "💰 MAYOREO" else "🛒 MENUDEO"
        val title = H3("🔍 Buscar Productos - $modeText")
        title.element.style.set("margin", "0")
        
        val closeButton = Button(Icon(VaadinIcon.CLOSE)) {
            dialog.close()
        }
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON)
        closeButton.element.setAttribute("aria-label", "Cerrar")
        
        topRow.add(title, closeButton)
        
        headerContainer.add(topRow)
        
        return headerContainer
    }
    
    private fun createBrandButtons(): HorizontalLayout {
        val layout = HorizontalLayout()
        layout.setWidthFull()
        layout.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        layout.isSpacing = true
        layout.element.style.set("gap", "20px")
        layout.element.style.set("padding", "20px 0")
        
        brands.forEach { brand ->
            val button = createBrandButton(brand)
            layout.add(button)
        }
        
        return layout
    }
    
    private fun createBrandButton(brand: Brand): Button {
        val button = Button(brand.name)
        
        // Estilos para hacer el botón grande y atractivo para tablet
        button.element.style.set("min-width", "200px")
        button.element.style.set("min-height", "80px")
        button.element.style.set("font-size", "1.4em")
        button.element.style.set("font-weight", "bold")
        button.element.style.set("border-radius", "12px")
        button.element.style.set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)")
        button.element.style.set("transition", "all 0.3s ease")
        
        // Colores específicos por marca
        when (brand.name) {
            "Perro Amor" -> {
                button.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                button.element.style.set("background", "linear-gradient(135deg, #e87ba7, #d15287)")
                button.element.style.set("color", "white")
            }
            "Perra Madre" -> {
                button.addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                button.element.style.set("background", "linear-gradient(135deg, #e8e320, #b6b119)")
                button.element.style.set("color", "white")
            }
            "Pashminas" -> {
                button.addThemeVariants(ButtonVariant.LUMO_CONTRAST)
                button.element.style.set("background", "linear-gradient(135deg, #ff9f43, #ee5a24)")
                button.element.style.set("color", "white")
            }
            else -> {
                // Color por defecto para nuevas marcas
                button.addThemeVariants(ButtonVariant.LUMO_CONTRAST)
                button.element.style.set("background", "linear-gradient(135deg, #2c5282, #2a4365)")
                button.element.style.set("color", "white")
            }
        }
        
        // Efecto hover mejorado
        button.element.addEventListener("mouseover") { _ ->
            button.element.style.set("transform", "translateY(-2px)")
            button.element.style.set("box-shadow", "0 6px 12px rgba(0,0,0,0.15)")
        }
        
        button.element.addEventListener("mouseout") { _ ->
            button.element.style.set("transform", "translateY(0)")
            button.element.style.set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)")
        }
        
        button.addClickListener {
            selectBrand(brand)
        }
        
        return button
    }
    
    private fun selectBrand(brand: Brand) {
        currentSelectedBrand = brand
        selectedBrandLabel.text = "Productos de: ${brand.name}"
        brandButtonsLayout.isVisible = false
        loadProductsForBrand(brand)
    }
    
    private fun loadProductsForBrand(brand: Brand) {
        // Limpiar área de productos
        productsGridArea.removeAll()
        
        // NO limpiar todos los contadores, solo los de la marca anterior
        // Los contadores se recrearán automáticamente al crear las tarjetas
        
        // Obtener productos de la marca seleccionada
        val products = productService.findByBrand(brand.name)
        
        if (products.isEmpty()) {
            val emptyMessage = Span("No hay productos disponibles para esta marca")
            emptyMessage.element.style.set("text-align", "center")
            emptyMessage.element.style.set("font-style", "italic")
            emptyMessage.element.style.set("color", "var(--lumo-secondary-text-color)")
            emptyMessage.element.style.set("padding", "40px")
            emptyMessage.element.style.set("font-size", "1.1em")
            productsGridArea.add(emptyMessage)
            return
        }
        
        // Crear grid de productos
        val gridContainer = createProductsGrid(products)
        productsGridArea.add(gridContainer)
        
        // Actualizar contadores para productos ya seleccionados de esta marca
        products.forEach { product ->
            if (selectedProducts.containsKey(product.id)) {
                updateProductCardCounter(product)
            }
        }
    }
    
    private fun createProductsGrid(products: List<Product>): Div {
        val gridContainer = Div()
        gridContainer.setWidthFull()
        
        // Configurar CSS Grid para una cuadrícula de 5 columnas
        gridContainer.element.style.set("display", "grid")
        gridContainer.element.style.set("grid-template-columns", "repeat(5, 1fr)")
        gridContainer.element.style.set("gap", "16px")
        gridContainer.element.style.set("padding", "16px")
        
        products.forEach { product ->
            val productCard = createProductCard(product)
            gridContainer.add(productCard)
        }
        
        return gridContainer
    }
    
    private fun createProductCard(product: Product): Div {
        val card = Div()
        
        // Estilos de la tarjeta
        card.element.style.set("border", "1px solid var(--lumo-contrast-10pct)")
        card.element.style.set("border-radius", "8px")
        card.element.style.set("padding", "16px")
        card.element.style.set("background", "var(--lumo-base-color)")
        card.element.style.set("cursor", "pointer")
        card.element.style.set("transition", "all 0.3s ease")
        card.element.style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
        card.element.style.set("min-height", "140px")
        card.element.style.set("display", "flex")
        card.element.style.set("flex-direction", "column")
        card.element.style.set("justify-content", "space-between")
        
        // Nombre del producto
        val productName = Span(product.name)
        productName.element.style.set("font-weight", "bold")
        productName.element.style.set("font-size", "1.1em")
        productName.element.style.set("color", "var(--lumo-primary-text-color)")
        productName.element.style.set("margin-bottom", "8px")
        productName.element.style.set("line-height", "1.3")
        
        // Información adicional
        val categorySpan = Span(product.category)
        categorySpan.element.style.set("font-size", "0.9em")
        categorySpan.element.style.set("color", "var(--lumo-secondary-text-color)")
        categorySpan.element.style.set("margin-bottom", "4px")
        
        val currentPrice = if (isWholesaleMode) product.wholesalePrice else product.price
        val priceLabel = if (isWholesaleMode) "Mayoreo" else "Regular"
        
        val priceField = NumberField()
        priceField.value = currentPrice.toDouble()
        priceField.width = "100px"
        priceField.element.style.set("font-size", "1.1em")
        priceField.prefixComponent = Span("$")
        
        val priceLabelSpan = Span("(${priceLabel})")
        priceLabelSpan.element.style.set("font-size", "0.9em")
        priceLabelSpan.element.style.set("color", "var(--lumo-secondary-text-color)")
        
        // Indicador de stock
        val stockIndicator = Span(
            if (product.stock <= 5) "⚠️ Stock Bajo (${product.stock})"
            else "✅ Stock: ${product.stock}"
        )
        stockIndicator.element.style.set("font-size", "0.8em")
        stockIndicator.element.style.set(
            "color", 
            if (product.stock <= 5) "var(--lumo-error-text-color)" 
            else "var(--lumo-success-text-color)"
        )
        
        // Indicador de variantes si aplica
        val variantInfo = if (product.hasVariants) {
            val variantSpan = Span("🔀 Tiene variantes")
            variantSpan.element.style.set("font-size", "0.8em")
            variantSpan.element.style.set("color", "var(--lumo-primary-text-color)")
            variantSpan
        } else null
        
        // Contenedor de información del producto
        val productInfo = VerticalLayout()
        productInfo.isSpacing = false
        productInfo.isPadding = false
        productInfo.add(productName, categorySpan)
        variantInfo?.let { productInfo.add(it) }
        
        // Contenedor de precio
        val priceContainer = HorizontalLayout()
        priceContainer.isSpacing = false
        priceContainer.isPadding = false
        priceContainer.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, priceField, priceLabelSpan)
        priceContainer.add(priceField, priceLabelSpan)
        
        // Contenedor de precio y stock
        val priceStockInfo = VerticalLayout()
        priceStockInfo.isSpacing = false
        priceStockInfo.isPadding = false
        priceStockInfo.add(priceContainer, stockIndicator)
        
        // Contador de productos seleccionados (inicialmente oculto)
        val counterBadge = Span("")
        counterBadge.element.style.set("position", "absolute")
        counterBadge.element.style.set("top", "8px")
        counterBadge.element.style.set("right", "8px")
        counterBadge.element.style.set("background", "var(--lumo-error-color)")
        counterBadge.element.style.set("color", "white")
        counterBadge.element.style.set("border-radius", "50%")
        counterBadge.element.style.set("width", "24px")
        counterBadge.element.style.set("height", "24px")
        counterBadge.element.style.set("display", "flex")
        counterBadge.element.style.set("align-items", "center")
        counterBadge.element.style.set("justify-content", "center")
        counterBadge.element.style.set("font-weight", "bold")
        counterBadge.element.style.set("font-size", "0.9em")
        counterBadge.element.style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.3)")
        counterBadge.isVisible = false
        
        // Agregar al mapa de contadores (reemplazar si ya existe)
        productCounterElements[product.id] = counterBadge
        
        // Hacer la tarjeta posicionable para el badge absoluto
        card.element.style.set("position", "relative")
        
        card.add(productInfo, priceStockInfo, counterBadge)
        
        // Efectos hover
        card.element.addEventListener("mouseover") { _ ->
            card.element.style.set("transform", "translateY(-2px)")
            card.element.style.set("box-shadow", "0 4px 8px rgba(0,0,0,0.15)")
            card.element.style.set("border-color", "var(--lumo-primary-color)")
        }
        
        card.element.addEventListener("mouseout") { _ ->
            card.element.style.set("transform", "translateY(0)")
            card.element.style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            card.element.style.set("border-color", "var(--lumo-contrast-10pct)")
        }
        
        // Click handler
        card.element.addEventListener("click") { _ ->
            addProductToSelection(product, priceField)
        }
        
        return card
    }
    
    private fun addProductToSelection(product: Product, priceField: NumberField) {
        val currentPrice = BigDecimal(priceField.value ?: product.price.toDouble())
        
        val existingProduct = selectedProducts[product.id]
        
        if (existingProduct != null) {
            // Incrementar cantidad del producto existente
            val newQuantity = existingProduct.quantity + 1
            
            // Validar stock
            if (product.stock > 0 && newQuantity > product.stock) {
                Notification.show(
                    "No hay suficiente stock disponible. Stock actual: ${product.stock}",
                    3000,
                    Notification.Position.TOP_CENTER
                )
                return
            }
            
            selectedProducts[product.id] = existingProduct.copy(
                quantity = newQuantity,
                unitPrice = currentPrice
            )
        } else {
            // Agregar nuevo producto
            if (product.stock > 0 && product.stock < 1) {
                Notification.show(
                    "No hay stock disponible",
                    3000,
                    Notification.Position.TOP_CENTER
                )
                return
            }
            
            selectedProducts[product.id] = ProductSelectionData(
                product = product,
                quantity = 1,
                unitPrice = currentPrice
            )
        }
        
        updateSelectedProductsTable()
        
        // Actualizar contador en la tarjeta del producto
        updateProductCardCounter(product)
        
        // Mostrar botón flotante si hay productos seleccionados
        updateFloatingButtonVisibility()
        
        Notification.show(
            "Producto agregado: ${product.name}",
            2000,
            Notification.Position.TOP_CENTER
        )
    }
    
    private fun setupComponents() {
        // Configuración inicial - mostrar instrucciones
        productsGridArea.removeAll()
        val instructionDiv = Div()
        instructionDiv.setWidthFull()
        instructionDiv.element.style.set("display", "flex")
        instructionDiv.element.style.set("flex-direction", "column")
        instructionDiv.element.style.set("justify-content", "center")
        instructionDiv.element.style.set("align-items", "center")
        instructionDiv.element.style.set("height", "100%")
        instructionDiv.element.style.set("min-height", "200px")
        
        val instructionIcon = Span("👆")
        instructionIcon.element.style.set("font-size", "3em")
        instructionIcon.element.style.set("margin-bottom", "16px")
        
        val instructionText = Span("Toca un botón de marca para ver los productos")
        instructionText.element.style.set("font-size", "1.2em")
        instructionText.element.style.set("color", "var(--lumo-secondary-text-color)")
        instructionText.element.style.set("text-align", "center")
        
        instructionDiv.add(instructionIcon, instructionText)
        productsGridArea.add(instructionDiv)
    }
    
    
    private fun createFloatingAddToSaleButton(): Button {
        val button = Button("Agregar a Venta", Icon(VaadinIcon.CART)) {
            confirmSelectedProducts()
        }
        
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS)
        button.element.style.set("position", "sticky")
        button.element.style.set("bottom", "16px")
        button.element.style.set("align-self", "center")
        button.element.style.set("z-index", "1000")
        button.element.style.set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
        button.element.style.set("font-size", "1.2em")
        button.element.style.set("font-weight", "bold")
        button.element.style.set("padding", "12px 24px")
        button.element.style.set("margin", "16px 0")
        
        button.isVisible = false
        
        return button
    }
    
    private fun updateSelectedProductsTable() {
        // Ya no hay tabla que actualizar - solo mantenemos la función por compatibilidad
        // La lógica se maneja directamente en el NewSaleView
    }
    
    private fun updateProductCardCounter(product: Product) {
        val counterElement = productCounterElements[product.id]
        val selectedProduct = selectedProducts[product.id]
        
        if (counterElement != null) {
            if (selectedProduct != null && selectedProduct.quantity > 0) {
                counterElement.text = selectedProduct.quantity.toString()
                counterElement.isVisible = true
            } else {
                counterElement.isVisible = false
            }
        }
    }
    
    private fun updateFloatingButtonVisibility() {
        addToSaleButton.isVisible = selectedProducts.isNotEmpty()
        
        // Actualizar el texto del botón con la cantidad
        if (selectedProducts.isNotEmpty()) {
            val totalQuantity = selectedProducts.values.sumOf { it.quantity }
            addToSaleButton.text = "Agregar a Venta ($totalQuantity)"
        }
    }
    
    
    private fun confirmSelectedProducts() {
        if (selectedProducts.isEmpty()) {
            Notification.show(
                "No hay productos seleccionados",
                2000,
                Notification.Position.TOP_CENTER
            )
            return
        }
        
        // Enviar todos los productos seleccionados
        selectedProducts.values.forEach { productData ->
            onProductSelectCallback?.invoke(productData)
        }
        
        // Cerrar el diálogo directamente
        dialog.close()
        
        Notification.show(
            "Se agregaron ${selectedProducts.size} producto(s) a la venta",
            3000,
            Notification.Position.TOP_CENTER
        )
    }
    
    
    private fun loadBrands() {
        brands = brandService.findActive().sortedByDescending { it.id }
        
        // Inicializar marcas por defecto si no hay ninguna
        if (brands.isEmpty()) {
            brandService.initializeDefaultBrands()
            brands = brandService.findActive()
        }
    }
}