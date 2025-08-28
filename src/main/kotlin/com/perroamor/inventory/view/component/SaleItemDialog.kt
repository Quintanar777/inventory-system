package com.perroamor.inventory.view.component

import com.perroamor.inventory.entity.Product
import com.perroamor.inventory.entity.ProductVariant
import com.perroamor.inventory.service.ProductService
import com.perroamor.inventory.service.ProductVariantService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.textfield.BigDecimalField
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class SaleItemDialog(
    @Autowired private val productService: ProductService,
    @Autowired private val variantService: ProductVariantService
) {
    
    data class SaleItemData(
        val product: Product,
        val variant: ProductVariant? = null,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val personalization: String? = null
    ) {
        fun getTotalPrice(): BigDecimal {
            return unitPrice.multiply(BigDecimal(quantity))
        }
        
        fun getDisplayName(): String {
            return if (variant != null) {
                "${product.name} - ${variant.variantName}"
            } else {
                product.name
            }
        }
    }
    
    private lateinit var dialog: Dialog
    private val brandFilter = RadioButtonGroup<String>()
    private val productSelector = ComboBox<Product>("Producto")
    private val variantSelector = ComboBox<ProductVariant>("Variante (Opcional)")
    private val quantityField = IntegerField("Cantidad")
    private val unitPriceField = BigDecimalField("Precio Unitario")
    private val personalizationField = TextField("Personalización (Opcional)")
    private val totalLabel = Span("Total: $0.00")
    
    private val brands = listOf("Perro Amor", "Perra Madre")
    
    private var onSaveCallback: ((SaleItemData) -> Unit)? = null
    
    fun show(onSave: (SaleItemData) -> Unit) {
        this.onSaveCallback = onSave
        createDialog()
        setupComponents()
        dialog.open()
    }
    
    private fun createDialog() {
        dialog = Dialog()
        dialog.isCloseOnEsc = true
        dialog.isCloseOnOutsideClick = false
        dialog.width = "500px"
        
        // Crear sección de filtro de marca
        val brandSection = VerticalLayout()
        brandSection.add(Span("Filtrar por Marca:"))
        brandSection.add(brandFilter)
        brandSection.isSpacing = false
        brandSection.isPadding = false
        
        val formLayout = FormLayout()
        formLayout.add(
            productSelector,
            variantSelector,
            quantityField,
            unitPriceField,
            personalizationField
        )
        
        val totalLayout = HorizontalLayout()
        totalLabel.style.set("font-weight", "bold")
        totalLabel.style.set("font-size", "1.2em")
        totalLayout.add(totalLabel)
        totalLayout.justifyContentMode = FlexComponent.JustifyContentMode.END
        
        val buttonsLayout = HorizontalLayout()
        
        val addButton = Button("Agregar a Venta") {
            addToSale()
        }
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        val cancelButton = Button("Cancelar") {
            dialog.close()
        }
        
        buttonsLayout.add(addButton, cancelButton)
        
        val content = VerticalLayout(
            H3("Agregar Producto a la Venta"),
            brandSection,
            formLayout,
            totalLayout,
            buttonsLayout
        )
        
        dialog.add(content)
    }
    
    private fun setupComponents() {
        setupBrandFilter()
        setupProductSelector()
        setupVariantSelector()
        setupQuantityField()
        setupPriceField()
        setupPersonalizationField()
        clearForm()
        loadProducts()
    }
    
    private fun setupBrandFilter() {
        brandFilter.setItems(brands)
        brandFilter.value = "Perro Amor" // Por defecto mostrar productos de Perro Amor
        brandFilter.addValueChangeListener { event ->
            event.value?.let { selectedBrand ->
                loadProductsByBrand(selectedBrand)
                // Limpiar selección de producto y variante cuando cambie la marca
                productSelector.clear()
                variantSelector.clear()
                variantSelector.setItems(emptyList())
                unitPriceField.value = BigDecimal.ZERO
                updateTotal()
            }
        }
    }
    
    private fun setupProductSelector() {
        productSelector.setItemLabelGenerator { "${it.name} - ${it.brand} (${it.category}) - $${it.price}" }
        productSelector.isAllowCustomValue = false
        productSelector.placeholder = "Buscar producto..."
        
        productSelector.addValueChangeListener { event ->
            val selectedProduct = event.value
            if (selectedProduct != null) {
                // Cargar variantes del producto
                loadVariants(selectedProduct)
                // Establecer precio por defecto
                unitPriceField.value = selectedProduct.price
                // Limpiar variante seleccionada
                variantSelector.clear()
                updateTotal()
            } else {
                variantSelector.clear()
                variantSelector.setItems(emptyList())
                unitPriceField.value = BigDecimal.ZERO
                updateTotal()
            }
        }
    }
    
    private fun setupVariantSelector() {
        variantSelector.setItemLabelGenerator { variant ->
            "${variant.variantName} - $${variant.getEffectivePrice()} (Stock: ${variant.stock})"
        }
        variantSelector.isAllowCustomValue = false
        variantSelector.placeholder = "Seleccionar variante (opcional)..."
        
        variantSelector.addValueChangeListener { event ->
            val selectedVariant = event.value
            if (selectedVariant != null) {
                // Actualizar precio con el de la variante
                unitPriceField.value = selectedVariant.getEffectivePrice()
            } else if (productSelector.value != null) {
                // Volver al precio del producto
                unitPriceField.value = productSelector.value.price
            }
            updateTotal()
        }
    }
    
    private fun setupQuantityField() {
        quantityField.value = 1
        quantityField.min = 1
        quantityField.max = 999
        
        quantityField.addValueChangeListener {
            updateTotal()
        }
    }
    
    private fun setupPriceField() {
        unitPriceField.value = BigDecimal.ZERO
        unitPriceField.placeholder = "0.00"
        
        unitPriceField.addValueChangeListener {
            updateTotal()
        }
    }
    
    private fun setupPersonalizationField() {
        personalizationField.placeholder = "Ej: Grabado de nombre 'Max'"
        personalizationField.helperText = "Servicios adicionales como grabado de nombre o teléfono"
    }
    
    private fun loadProducts() {
        val selectedBrand = brandFilter.value ?: "Perro Amor"
        loadProductsByBrand(selectedBrand)
    }
    
    private fun loadProductsByBrand(brand: String) {
        val products = productService.findByBrand(brand)
        productSelector.setItems(products)
    }
    
    private fun loadVariants(product: Product) {
        if (product.hasVariants) {
            val variants = variantService.findActiveByProduct(product)
            variantSelector.isEnabled = true
            variantSelector.setItems(variants)
            variantSelector.helperText = if (variants.isNotEmpty()) {
                "Seleccione una variante específica (${variants.size} disponibles)"
            } else {
                "No hay variantes activas para este producto"
            }
        } else {
            variantSelector.isEnabled = false
            variantSelector.setItems(emptyList())
            variantSelector.helperText = "Este producto no tiene variantes"
        }
    }
    
    private fun updateTotal() {
        val quantity = quantityField.value ?: 0
        val unitPrice = unitPriceField.value ?: BigDecimal.ZERO
        val total = unitPrice.multiply(BigDecimal(quantity))
        
        totalLabel.text = "Total: $${total}"
    }
    
    private fun addToSale() {
        try {
            validateForm()
            
            val saleItemData = SaleItemData(
                product = productSelector.value,
                variant = variantSelector.value,
                quantity = quantityField.value,
                unitPrice = unitPriceField.value,
                personalization = if (personalizationField.value.isBlank()) null else personalizationField.value
            )
            
            onSaveCallback?.invoke(saleItemData)
            dialog.close()
            
            Notification.show(
                "Producto agregado: ${saleItemData.getDisplayName()}",
                3000,
                Notification.Position.TOP_CENTER
            )
            
        } catch (e: Exception) {
            Notification.show(
                "Error: ${e.message}",
                3000,
                Notification.Position.TOP_CENTER
            )
        }
    }
    
    private fun validateForm() {
        require(productSelector.value != null) { "Debe seleccionar un producto" }
        require(quantityField.value > 0) { "La cantidad debe ser mayor a 0" }
        require(unitPriceField.value >= BigDecimal.ZERO) { "El precio no puede ser negativo" }
        
        // Validar stock si hay variante seleccionada
        val selectedVariant = variantSelector.value
        if (selectedVariant != null) {
            require(quantityField.value <= selectedVariant.stock) {
                "No hay suficiente stock. Disponible: ${selectedVariant.stock}"
            }
        } else {
            // Validar stock del producto
            val selectedProduct = productSelector.value
            if (selectedProduct.stock > 0) { // Solo validar si el producto maneja stock
                require(quantityField.value <= selectedProduct.stock) {
                    "No hay suficiente stock. Disponible: ${selectedProduct.stock}"
                }
            }
        }
    }
    
    private fun clearForm() {
        productSelector.clear()
        variantSelector.clear()
        quantityField.value = 1
        unitPriceField.value = BigDecimal.ZERO
        personalizationField.clear()
        updateTotal()
    }
}