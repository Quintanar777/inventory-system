package com.perroamor.inventory.view

import com.perroamor.inventory.entity.Product
import com.perroamor.inventory.entity.ProductVariant
import com.perroamor.inventory.service.ProductService
import com.perroamor.inventory.service.ProductVariantService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.BigDecimalField
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.*
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

@Route("variants", layout = MainLayout::class)
@RouteAlias("variants/:productId", layout = MainLayout::class)
@PageTitle("Variantes de Productos")
class ProductVariantView(
    @Autowired private val productService: ProductService,
    @Autowired private val variantService: ProductVariantService
) : VerticalLayout(), BeforeEnterObserver {
    
    private val productSelector = ComboBox<Product>("Producto")
    private val variantGrid = Grid(ProductVariant::class.java)
    
    private val variantNameField = TextField("Nombre de Variante")
    private val colorField = TextField("Color")
    private val designField = TextField("Diseño")
    private val materialField = TextField("Material")
    private val sizeField = ComboBox<String>("Talla")
    private val priceAdjustmentField = BigDecimalField("Ajuste de Precio")
    private val stockField = IntegerField("Stock")
    private val skuField = TextField("SKU")
    private val descriptionField = TextField("Descripción")
    private val shopifyVariantIdField = TextField("Shopify Variant ID")
    private val shopifyProductIdField = TextField("Shopify Product ID")
    private val isActiveCheckbox = Checkbox("Activa")
    
    private val sizes = mutableListOf(
        "XS", "S", "M", "L", "XL", "XXL",
        "Pequeño", "Mediano", "Grande",
        "Cachorro", "Adulto", 
        "Único", "Unitalla"
    )
    
    init {
        setSizeFull()
        setupProductSelector()
        configureSizeField()
        configureShopifyFields()
        configureVariantGrid()
        
        add(
            H2("Gestión de Variantes de Productos"),
            createToolbar(),
            productSelector,
            variantGrid
        )
        
        updateProductList()
    }
    
    private fun setupProductSelector() {
        productSelector.setItemLabelGenerator { "${it.name} (${it.category})" }
        productSelector.addValueChangeListener { event ->
            event.value?.let { product ->
                updateVariantGrid(product)
            } ?: variantGrid.setItems(emptyList())
        }
    }
    
    private fun configureSizeField() {
        sizeField.setItems(sizes)
        sizeField.isAllowCustomValue = true
        sizeField.addCustomValueSetListener { event ->
            val customValue = event.detail
            if (customValue.isNotBlank() && !sizes.contains(customValue)) {
                sizes.add(customValue)
                sizeField.setItems(sizes)
                sizeField.value = customValue
            }
        }
    }
    
    private fun configureShopifyFields() {
        shopifyVariantIdField.placeholder = "gid://shopify/ProductVariant/123456789"
        shopifyVariantIdField.helperText = "ID único de la variante en Shopify"
        
        shopifyProductIdField.placeholder = "gid://shopify/Product/987654321"
        shopifyProductIdField.helperText = "ID del producto padre en Shopify"
    }
    
    private fun configureVariantGrid() {
        variantGrid.setSizeFull()
        variantGrid.removeAllColumns()
        
        variantGrid.addColumn(ProductVariant::variantName).setHeader("Variante")
        variantGrid.addColumn(ProductVariant::color).setHeader("Color")
        variantGrid.addColumn(ProductVariant::design).setHeader("Diseño")
        variantGrid.addColumn(ProductVariant::size).setHeader("Talla")
        variantGrid.addColumn { it.getEffectivePrice() }.setHeader("Precio Final")
        variantGrid.addColumn(ProductVariant::stock).setHeader("Stock")
        variantGrid.addColumn { if (it.isActive) "✅ Activa" else "❌ Inactiva" }.setHeader("Estado")
        
        variantGrid.addColumn { variant ->
            if (variant.stock <= 5) "⚠️ BAJO" else "✅ OK"
        }.setHeader("Estado Stock")
        
        variantGrid.asSingleSelect().addValueChangeListener { event ->
            event.value?.let { editVariant(it) }
        }
    }
    
    private fun createToolbar(): HorizontalLayout {
        val addVariantButton = Button("Nueva Variante") { 
            productSelector.value?.let { addVariant(it) } 
                ?: Notification.show("Selecciona un producto primero", 3000, Notification.Position.TOP_CENTER)
        }
        addVariantButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        val refreshButton = Button("Actualizar") { 
            productSelector.value?.let { updateVariantGrid(it) }
        }
        
        return HorizontalLayout(addVariantButton, refreshButton)
    }
    
    private fun addVariant(product: Product) {
        showVariantDialog(product, null)
    }
    
    private fun editVariant(variant: ProductVariant) {
        showVariantDialog(variant.product, variant)
    }
    
    private fun showVariantDialog(product: Product, variant: ProductVariant?) {
        val dialog = Dialog()
        val formLayout = FormLayout()
        
        if (variant != null) {
            variantNameField.value = variant.variantName
            colorField.value = variant.color ?: ""
            designField.value = variant.design ?: ""
            materialField.value = variant.material ?: ""
            sizeField.value = variant.size ?: ""
            priceAdjustmentField.value = variant.priceAdjustment
            stockField.value = variant.stock
            skuField.value = variant.sku ?: ""
            descriptionField.value = variant.description ?: ""
            shopifyVariantIdField.value = variant.shopifyVariantId ?: ""
            shopifyProductIdField.value = variant.shopifyProductId ?: ""
            isActiveCheckbox.value = variant.isActive
        } else {
            clearVariantForm()
        }
        
        formLayout.add(
            variantNameField, colorField, designField, materialField, sizeField,
            priceAdjustmentField, stockField, skuField, descriptionField, 
            shopifyVariantIdField, shopifyProductIdField, isActiveCheckbox
        )
        
        val saveButton = Button("Guardar") {
            saveVariant(product, variant)
            dialog.close()
        }
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        val cancelButton = Button("Cancelar") { dialog.close() }
        
        val buttonLayout = HorizontalLayout(saveButton, cancelButton)
        
        dialog.add(
            VerticalLayout(
                if (variant != null) H3("Editar Variante") else H3("Nueva Variante"),
                H3("Producto: ${product.name}"),
                formLayout,
                buttonLayout
            )
        )
        
        dialog.open()
    }
    
    private fun saveVariant(product: Product, existingVariant: ProductVariant?) {
        try {
            val variant = if (existingVariant != null) {
                existingVariant.copy(
                    variantName = variantNameField.value,
                    color = if (colorField.value.isBlank()) null else colorField.value,
                    design = if (designField.value.isBlank()) null else designField.value,
                    material = if (materialField.value.isBlank()) null else materialField.value,
                    size = if (sizeField.value.isBlank()) null else sizeField.value,
                    priceAdjustment = priceAdjustmentField.value,
                    stock = stockField.value,
                    sku = if (skuField.value.isBlank()) null else skuField.value,
                    description = if (descriptionField.value.isBlank()) null else descriptionField.value,
                    shopifyVariantId = if (shopifyVariantIdField.value.isBlank()) null else shopifyVariantIdField.value,
                    shopifyProductId = if (shopifyProductIdField.value.isBlank()) null else shopifyProductIdField.value,
                    isActive = isActiveCheckbox.value
                )
            } else {
                ProductVariant(
                    product = product,
                    variantName = variantNameField.value,
                    color = if (colorField.value.isBlank()) null else colorField.value,
                    design = if (designField.value.isBlank()) null else designField.value,
                    material = if (materialField.value.isBlank()) null else materialField.value,
                    size = if (sizeField.value.isBlank()) null else sizeField.value,
                    priceAdjustment = priceAdjustmentField.value,
                    stock = stockField.value,
                    sku = if (skuField.value.isBlank()) null else skuField.value,
                    description = if (descriptionField.value.isBlank()) null else descriptionField.value,
                    shopifyVariantId = if (shopifyVariantIdField.value.isBlank()) null else shopifyVariantIdField.value,
                    shopifyProductId = if (shopifyProductIdField.value.isBlank()) null else shopifyProductIdField.value,
                    isActive = isActiveCheckbox.value
                )
            }
            
            variantService.save(variant)
            updateVariantGrid(product)
            clearVariantForm()
            
            // Actualizar el producto para que tenga variantes
            if (!product.hasVariants) {
                val updatedProduct = product.copy(hasVariants = true)
                productService.save(updatedProduct)
                updateProductList()
            }
            
            Notification.show(
                if (existingVariant != null) "Variante actualizada" else "Variante agregada",
                3000,
                Notification.Position.TOP_CENTER
            )
        } catch (e: Exception) {
            Notification.show("Error: ${e.message}", 3000, Notification.Position.TOP_CENTER)
        }
    }
    
    private fun clearVariantForm() {
        variantNameField.clear()
        colorField.clear()
        designField.clear()
        materialField.clear()
        sizeField.clear()
        priceAdjustmentField.value = BigDecimal.ZERO
        stockField.value = 0
        skuField.clear()
        descriptionField.clear()
        shopifyVariantIdField.clear()
        shopifyProductIdField.clear()
        isActiveCheckbox.value = true
    }
    
    private fun updateProductList() {
        val allProducts = productService.findAll()
        // Filtrar solo productos que tienen o pueden tener variantes
        productSelector.setItems(allProducts)
    }
    
    private fun updateVariantGrid(product: Product) {
        val variants = variantService.findByProductId(product.id)
        println("Cargando ${variants.size} variantes para ${product.name}")
        variantGrid.setItems(variants)
    }
    
    override fun beforeEnter(event: BeforeEnterEvent) {
        val productIdParam = event.routeParameters.get("productId")
        if (productIdParam.isPresent) {
            val productIdStr = productIdParam.get()
            try {
                val productId = productIdStr.toLong()
                val product = productService.findById(productId)
                if (product != null) {
                    // Auto-seleccionar el producto en el ComboBox
                    productSelector.value = product
                    updateVariantGrid(product)
                    Notification.show(
                        "Mostrando variantes de: ${product.name}",
                        3000,
                        Notification.Position.TOP_CENTER
                    )
                } else {
                    Notification.show(
                        "Producto no encontrado",
                        3000,
                        Notification.Position.MIDDLE
                    )
                }
            } catch (e: NumberFormatException) {
                Notification.show(
                    "ID de producto inválido",
                    3000,
                    Notification.Position.MIDDLE
                )
            }
        }
        // Si no hay parámetro, simplemente muestra la vista sin pre-seleccionar ningún producto
    }
}