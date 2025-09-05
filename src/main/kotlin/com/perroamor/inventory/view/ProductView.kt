package com.perroamor.inventory.view

import com.perroamor.inventory.entity.Product
import com.perroamor.inventory.entity.Brand
import com.perroamor.inventory.service.ProductService
import com.perroamor.inventory.service.BrandService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.BigDecimalField
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import jakarta.annotation.security.RolesAllowed
import java.math.BigDecimal

@Route("", layout = MainLayout::class)
@PageTitle("Inventario")
@RolesAllowed("ADMIN", "MANAGER")
class ProductView(
    private val productService: ProductService,
    private val brandService: BrandService
) : VerticalLayout() {

    private val grid = Grid(Product::class.java)
    private val nameField = TextField("Nombre")
    private val priceField = BigDecimalField("Precio Regular")
    private val wholesalePriceField = BigDecimalField("Precio Mayoreo")
    private val categoryField = ComboBox<String>("Categoría")
    private val brandField = ComboBox<Brand>("Marca")
    private val stockField = IntegerField("Stock")
    private val descriptionField = TextField("Descripción")

    private val categories = mutableListOf(
        "Collares",
        "Correas",
        "Mochilas",
        "Personalización",
        "Accesorios",
        "Juguetes"
    )

    // Las marcas ahora se cargan dinámicamente desde la base de datos

    init {
        setSizeFull()
        configureGrid()
        configureForm()

        add(
            createToolbar(),
            grid
        )

        updateList()
    }

    private fun configureGrid() {
        grid.setSizeFull()
        grid.setColumns("id", "name", "brand", "price", "wholesalePrice", "category", "stock")

        // Configurar anchos de columnas
        grid.getColumnByKey("id").setWidth("80px").setFlexGrow(0).setHeader("ID")
        grid.getColumnByKey("name").setHeader("Nombre").setFlexGrow(2)
        grid.getColumnByKey("brand").setHeader("Marca").setFlexGrow(1)
        grid.getColumnByKey("price").setHeader("Precio Regular").setWidth("130px").setFlexGrow(0)
        grid.getColumnByKey("wholesalePrice").setHeader("Precio Mayoreo").setWidth("130px").setFlexGrow(0)
        grid.getColumnByKey("category").setHeader("Categoría").setFlexGrow(1)
        grid.getColumnByKey("stock").setHeader("Stock").setWidth("100px").setFlexGrow(0)

        grid.addColumn { product ->
            if (product.hasVariants) "🔀 Sí" else "➖ No"
        }.setHeader("Tiene Variantes").setWidth("140px").setFlexGrow(0)

        grid.addColumn { product ->
            if (product.stock <= 5) "⚠️ BAJO" else "✅ OK"
        }.setHeader("Estado Stock").setWidth("130px").setFlexGrow(0)

        // Columna de acción para productos con variantes
        grid.addComponentColumn { product ->
            if (product.hasVariants) {
                Button("Ver Variantes", Icon(VaadinIcon.SPLIT)) {
                    navigateToVariants(product)
                }.apply {
                    addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY)
                }
            } else {
                Button("Crear Variantes", Icon(VaadinIcon.PLUS)) {
                    navigateToVariants(product)
                }.apply {
                    addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE)
                }
            }
        }.setHeader("Variantes").setWidth("160px").setFlexGrow(0)

        grid.asSingleSelect().addValueChangeListener { event ->
            event.value?.let { editProduct(it) }
        }
    }

    private fun configureForm() {
        priceField.value = BigDecimal.ZERO
        wholesalePriceField.value = BigDecimal.ZERO
        stockField.value = 0
        
        // Agregar listener para auto-calcular precio de mayoreo (85% del precio regular)
        priceField.addValueChangeListener { event ->
            val regularPrice = event.value
            if (regularPrice != null && regularPrice > BigDecimal.ZERO) {
                wholesalePriceField.value = regularPrice.multiply(BigDecimal("0.85"))
            }
        }

        categoryField.setItems(categories)
        categoryField.isAllowCustomValue = true
        categoryField.addCustomValueSetListener { event ->
            val customValue = event.detail
            if (customValue.isNotBlank() && !categories.contains(customValue)) {
                categories.add(customValue)
                categoryField.setItems(categories)
                categoryField.value = customValue
            }
        }

        updateBrandList()
        brandField.isAllowCustomValue = false
        brandField.setItemLabelGenerator { it.name }
    }

    private fun createToolbar(): HorizontalLayout {
        val addButton = Button("Agregar Producto") { addProduct() }
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)

        val refreshButton = Button("Actualizar") { updateList() }

        return HorizontalLayout(addButton, refreshButton)
    }

    private fun addProduct() {
        showProductDialog(null)
    }

    private fun editProduct(product: Product) {
        showProductDialog(product)
    }

    private fun showProductDialog(product: Product?) {
        val dialog = Dialog()
        val formLayout = FormLayout()

        if (product != null) {
            nameField.value = product.name
            priceField.value = product.price
            wholesalePriceField.value = product.wholesalePrice
            categoryField.value = product.category
            brandField.value = brandService.findByName(product.brand)
            stockField.value = product.stock
            descriptionField.value = product.description ?: ""
        } else {
            clearForm()
        }

        formLayout.add(nameField, priceField, wholesalePriceField, categoryField, brandField, stockField, descriptionField)

        val saveButton = Button("Guardar") {
            saveProduct(product)
            dialog.close()
        }
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)

        val cancelButton = Button("Cancelar") { dialog.close() }

        val buttonLayout = HorizontalLayout(saveButton, cancelButton)

        dialog.add(
            VerticalLayout(
                if (product != null) H1("Editar Producto") else H1("Nuevo Producto"),
                formLayout,
                buttonLayout
            )
        )

        dialog.open()
    }

    private fun saveProduct(existingProduct: Product?) {
        try {
            val product = if (existingProduct != null) {
                existingProduct.copy(
                    name = nameField.value,
                    price = priceField.value,
                    wholesalePrice = wholesalePriceField.value,
                    category = categoryField.value,
                    brand = brandField.value?.name ?: "",
                    stock = stockField.value,
                    description = if (descriptionField.value.isBlank()) null else descriptionField.value
                )
            } else {
                Product(
                    name = nameField.value,
                    price = priceField.value,
                    wholesalePrice = wholesalePriceField.value,
                    category = categoryField.value,
                    brand = brandField.value?.name ?: "",
                    stock = stockField.value,
                    description = if (descriptionField.value.isBlank()) null else descriptionField.value
                )
            }

            productService.save(product)
            updateList()
            clearForm()
            Notification.show(
                if (existingProduct != null) "Producto actualizado" else "Producto agregado",
                3000,
                Notification.Position.TOP_CENTER
            )
        } catch (e: Exception) {
            Notification.show("Error: ${e.message}", 3000, Notification.Position.TOP_CENTER)
        }
    }

    private fun clearForm() {
        nameField.clear()
        priceField.value = BigDecimal.ZERO
        wholesalePriceField.value = BigDecimal.ZERO
        categoryField.clear()
        brandField.clear()
        stockField.value = 0
        descriptionField.clear()
    }

    private fun navigateToVariants(product: Product) {
        // Navegar a la vista de variantes con el ID del producto como parámetro
        UI.getCurrent().navigate("variants/${product.id}")
    }

    private fun updateList() {
        grid.setItems(productService.findAll())
    }
    
    private fun updateBrandList() {
        val activeBrands = brandService.findActive()
        brandField.setItems(activeBrands)
        
        // Si hay marcas disponibles, seleccionar la primera como valor por defecto
        if (activeBrands.isNotEmpty()) {
            brandField.value = activeBrands.first()
        }
    }
}