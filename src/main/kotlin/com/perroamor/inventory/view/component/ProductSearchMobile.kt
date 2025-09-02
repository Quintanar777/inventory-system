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
    
    private var brands = listOf<Brand>()
    private var currentSelectedBrand: Brand? = null
    data class ProductSelectionData(
        val product: Product,
        val quantity: Int,
        val unitPrice: BigDecimal
    )
    
    private var onProductSelectCallback: ((ProductSelectionData) -> Unit)? = null
    
    fun show(onProductSelect: (ProductSelectionData) -> Unit) {
        this.onProductSelectCallback = onProductSelect
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
        productsGridArea.setSizeFull()
        productsGridArea.element.style.set("overflow", "auto")
        
        contentArea.add(
            headerLayout,
            brandButtonsLayout,
            selectedBrandLabel,
            productsGridArea
        )
        
        // Configurar flex grow para que el área de productos ocupe el espacio disponible
        contentArea.setFlexGrow(0.0, headerLayout)
        contentArea.setFlexGrow(0.0, brandButtonsLayout)
        contentArea.setFlexGrow(0.0, selectedBrandLabel)
        contentArea.setFlexGrow(1.0, productsGridArea)
        
        dialog.add(contentArea)
    }
    
    private fun createHeader(): HorizontalLayout {
        val headerLayout = HorizontalLayout()
        headerLayout.setWidthFull()
        headerLayout.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        headerLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER)
        
        val title = H3("🔍 Buscar Productos")
        title.element.style.set("margin", "0")
        
        val closeButton = Button(Icon(VaadinIcon.CLOSE)) {
            dialog.close()
        }
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON)
        closeButton.element.setAttribute("aria-label", "Cerrar")
        
        headerLayout.add(title, closeButton)
        
        return headerLayout
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
                button.element.style.set("background", "linear-gradient(135deg, #ff6b6b, #ee5a24)")
                button.element.style.set("color", "white")
            }
            "Perra Madre" -> {
                button.addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                button.element.style.set("background", "linear-gradient(135deg, #5f27cd, #341f97)")
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
        loadProductsForBrand(brand.name)
    }
    
    private fun loadProductsForBrand(brand: String) {
        // Limpiar área de productos
        productsGridArea.removeAll()
        
        // Obtener productos de la marca seleccionada
        val products = productService.findByBrand(brand)
        
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
    }
    
    private fun createProductsGrid(products: List<Product>): Div {
        val gridContainer = Div()
        gridContainer.setWidthFull()
        
        // Configurar CSS Grid para una cuadrícula responsiva
        gridContainer.element.style.set("display", "grid")
        gridContainer.element.style.set("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))")
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
        
        val priceSpan = Span("$${product.price}")
        priceSpan.element.style.set("font-weight", "bold")
        priceSpan.element.style.set("font-size", "1.2em")
        priceSpan.element.style.set("color", "var(--lumo-success-text-color)")
        
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
        
        // Contenedor de precio y stock
        val priceStockInfo = VerticalLayout()
        priceStockInfo.isSpacing = false
        priceStockInfo.isPadding = false
        priceStockInfo.add(priceSpan, stockIndicator)
        
        card.add(productInfo, priceStockInfo)
        
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
            selectProduct(product)
        }
        
        return card
    }
    
    private fun selectProduct(product: Product) {
        showProductConfirmDialog(product)
    }
    
    private fun showProductConfirmDialog(product: Product) {
        val confirmDialog = Dialog()
        confirmDialog.width = "400px"
        confirmDialog.isCloseOnEsc = true
        confirmDialog.isCloseOnOutsideClick = false
        
        val title = H3("Confirmar Producto")
        title.element.style.set("margin", "0 0 16px 0")
        title.element.style.set("color", "var(--lumo-primary-text-color)")
        
        // Información del producto
        val productInfoLayout = VerticalLayout()
        productInfoLayout.isSpacing = false
        productInfoLayout.isPadding = false
        
        val productName = Span(product.name)
        productName.element.style.set("font-weight", "bold")
        productName.element.style.set("font-size", "1.2em")
        productName.element.style.set("margin-bottom", "4px")
        
        val productDetails = Span("${product.brand} • ${product.category}")
        productDetails.element.style.set("color", "var(--lumo-secondary-text-color)")
        productDetails.element.style.set("font-size", "0.9em")
        
        productInfoLayout.add(productName, productDetails)
        
        // Campos editables
        val quantityField = IntegerField("Cantidad")
        quantityField.value = 1
        quantityField.min = 1
        quantityField.max = if (product.stock > 0) product.stock else 999
        quantityField.width = "120px"
        quantityField.element.style.set("font-size", "1.1em")
        
        val priceField = BigDecimalField("Precio Unitario")
        priceField.value = product.price
        //priceField.min = BigDecimal.ZERO
        priceField.width = "150px"
        priceField.element.style.set("font-size", "1.1em")
        priceField.prefixComponent = Span("$")
        
        // Total calculado
        val totalLabel = Span("Total: $${product.price}")
        totalLabel.element.style.set("font-weight", "bold")
        totalLabel.element.style.set("font-size", "1.3em")
        totalLabel.element.style.set("color", "var(--lumo-success-text-color)")
        totalLabel.element.style.set("margin-top", "8px")
        
        // Función para actualizar el total
        val updateTotal = {
            val quantity = quantityField.value ?: 1
            val price = priceField.value ?: BigDecimal.ZERO
            val total = price.multiply(BigDecimal(quantity))
            totalLabel.text = "Total: $$total"
        }
        
        quantityField.addValueChangeListener { updateTotal() }
        priceField.addValueChangeListener { updateTotal() }
        
        // Layout de campos
        val fieldsLayout = HorizontalLayout()
        fieldsLayout.add(quantityField, priceField)
        fieldsLayout.isSpacing = true
        fieldsLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END, quantityField, priceField)
        
        // Validación de stock
        val stockWarning = Span("")
        stockWarning.element.style.set("color", "var(--lumo-error-text-color)")
        stockWarning.element.style.set("font-size", "0.9em")
        stockWarning.element.style.set("margin-top", "4px")
        stockWarning.isVisible = false
        
        quantityField.addValueChangeListener { event ->
            val quantity = event.value ?: 1
            if (product.stock > 0 && quantity > product.stock) {
                stockWarning.text = "⚠️ Stock disponible: ${product.stock}"
                stockWarning.isVisible = true
                quantityField.value = product.stock
            } else {
                stockWarning.isVisible = false
            }
        }
        
        // Botones
        val buttonsLayout = HorizontalLayout()
        buttonsLayout.setWidthFull()
        buttonsLayout.justifyContentMode = FlexComponent.JustifyContentMode.END
        buttonsLayout.isSpacing = true
        buttonsLayout.element.style.set("margin-top", "20px")
        
        val cancelButton = Button("Cancelar") {
            confirmDialog.close()
        }
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        
        val confirmButton = Button("Agregar a Venta", Icon(VaadinIcon.CHECK)) {
            val finalQuantity = quantityField.value ?: 1
            val finalPrice = priceField.value ?: product.price
            
            // Validación final
            if (product.stock > 0 && finalQuantity > product.stock) {
                Notification.show(
                    "No hay suficiente stock disponible",
                    3000,
                    Notification.Position.TOP_CENTER
                )
                return@Button
            }
            
            val selectionData = ProductSelectionData(
                product = product,
                quantity = finalQuantity,
                unitPrice = finalPrice
            )
            
            onProductSelectCallback?.invoke(selectionData)
            confirmDialog.close()
            dialog.close()
            
            Notification.show(
                "Producto agregado: ${product.name} (${finalQuantity}x)",
                3000,
                Notification.Position.TOP_CENTER
            )
        }
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS)
        
        buttonsLayout.add(cancelButton, confirmButton)
        
        // Layout principal del diálogo
        val mainLayout = VerticalLayout()
        mainLayout.add(
            title,
            productInfoLayout,
            fieldsLayout,
            stockWarning,
            totalLabel,
            buttonsLayout
        )
        mainLayout.isPadding = true
        mainLayout.isSpacing = true
        
        confirmDialog.add(mainLayout)
        confirmDialog.open()
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
    
    private fun loadBrands() {
        brands = brandService.findActive()
        
        // Inicializar marcas por defecto si no hay ninguna
        if (brands.isEmpty()) {
            brandService.initializeDefaultBrands()
            brands = brandService.findActive()
        }
    }
}