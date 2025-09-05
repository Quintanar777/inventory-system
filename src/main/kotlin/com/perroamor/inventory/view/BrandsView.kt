package com.perroamor.inventory.view

import com.perroamor.inventory.entity.Brand
import com.perroamor.inventory.service.BrandService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import jakarta.annotation.security.RolesAllowed

@Route("brands", layout = MainLayout::class)
@PageTitle("Gestión de Marcas")
@RolesAllowed("ADMIN", "MANAGER")
class BrandsView(@Autowired private val brandService: BrandService) : VerticalLayout() {
    
    private val grid = Grid(Brand::class.java)
    private val nameField = TextField("Nombre de la Marca")
    private val descriptionField = TextArea("Descripción")
    private val activeField = Checkbox("Activa")
    
    init {
        setSizeFull()
        configureGrid()
        setupView()
        updateList()
        
        // Inicializar marcas por defecto si no existen
        brandService.initializeDefaultBrands()
    }
    
    private fun setupView() {
        add(
            createHeader(),
            createToolbar(),
            grid
        )
    }
    
    private fun createHeader(): VerticalLayout {
        val layout = VerticalLayout()
        layout.isSpacing = false
        layout.isPadding = false
        
        val title = H1("🏷️ Gestión de Marcas")
        title.element.style.set("margin", "0 0 8px 0")
        
        layout.add(title)
        return layout
    }
    
    private fun createToolbar(): HorizontalLayout {
        val addButton = Button("Nueva Marca") { addBrand() }
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        addButton.icon = Icon(VaadinIcon.PLUS)
        
        val refreshButton = Button("Actualizar") { updateList() }
        refreshButton.icon = Icon(VaadinIcon.REFRESH)
        
        val toolbar = HorizontalLayout(addButton, refreshButton)
        toolbar.element.style.set("margin-bottom", "16px")
        
        return toolbar
    }
    
    private fun configureGrid() {
        grid.setSizeFull()
        grid.removeAllColumns()
        
        // Configurar columnas
        grid.addColumn { it.id }
            .setHeader("ID")
            .setWidth("80px")
            .setFlexGrow(0)
        
        grid.addColumn { it.name }
            .setHeader("Nombre")
            .setFlexGrow(2)
        
        grid.addColumn { it.description ?: "-" }
            .setHeader("Descripción")
            .setFlexGrow(3)
        
        grid.addColumn { brand ->
            if (brand.isActive) "✅ Activa" else "❌ Inactiva"
        }.setHeader("Estado").setWidth("120px").setFlexGrow(0)
        
        grid.addColumn { brand ->
            "${brand.createdAt.dayOfMonth}/${brand.createdAt.monthValue}/${brand.createdAt.year}"
        }.setHeader("Creada").setWidth("100px").setFlexGrow(0)
        
        // Columna de acciones
        grid.addComponentColumn { brand ->
            createActionButtons(brand)
        }.setHeader("Acciones").setWidth("200px").setFlexGrow(0)
        
        grid.asSingleSelect().addValueChangeListener { event ->
            event.value?.let { editBrand(it) }
        }
    }
    
    private fun createActionButtons(brand: Brand): HorizontalLayout {
        val layout = HorizontalLayout()
        layout.isSpacing = true
        
        val editButton = Button(Icon(VaadinIcon.EDIT)) {
            editBrand(brand)
        }
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY)
        editButton.element.setAttribute("aria-label", "Editar")
        
        val toggleButton = if (brand.isActive) {
            Button(Icon(VaadinIcon.EYE_SLASH)) {
                toggleBrandStatus(brand, false)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY)
                element.setAttribute("aria-label", "Desactivar")
            }
        } else {
            Button(Icon(VaadinIcon.EYE)) {
                toggleBrandStatus(brand, true)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY)
                element.setAttribute("aria-label", "Activar")
            }
        }
        
        layout.add(editButton, toggleButton)
        return layout
    }
    
    private fun addBrand() {
        showBrandDialog(null)
    }
    
    private fun editBrand(brand: Brand) {
        showBrandDialog(brand)
    }
    
    private fun showBrandDialog(brand: Brand?) {
        val dialog = Dialog()
        dialog.width = "500px"
        
        val formLayout = FormLayout()
        
        // Configurar campos
        if (brand != null) {
            nameField.value = brand.name
            descriptionField.value = brand.description ?: ""
            activeField.value = brand.isActive
        } else {
            clearForm()
        }
        
        descriptionField.height = "120px"
        descriptionField.placeholder = "Descripción opcional de la marca..."
        
        formLayout.add(nameField, descriptionField, activeField)
        
        // Botones
        val saveButton = Button("Guardar") {
            saveBrand(brand)
            dialog.close()
        }
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        val cancelButton = Button("Cancelar") { 
            dialog.close() 
        }
        
        val buttonLayout = HorizontalLayout(saveButton, cancelButton)
        
        val mainLayout = VerticalLayout(
            if (brand != null) H3("Editar Marca") else H3("Nueva Marca"),
            formLayout,
            buttonLayout
        )
        
        dialog.add(mainLayout)
        dialog.open()
    }
    
    private fun saveBrand(existingBrand: Brand?) {
        try {
            val brand = if (existingBrand != null) {
                existingBrand.copy(
                    name = nameField.value,
                    description = if (descriptionField.value.isBlank()) null else descriptionField.value,
                    isActive = activeField.value
                )
            } else {
                // Verificar que no existe una marca con el mismo nombre
                if (brandService.existsByName(nameField.value)) {
                    Notification.show(
                        "Ya existe una marca con ese nombre",
                        3000,
                        Notification.Position.TOP_CENTER
                    )
                    return
                }
                
                Brand(
                    name = nameField.value,
                    description = if (descriptionField.value.isBlank()) null else descriptionField.value,
                    isActive = activeField.value
                )
            }
            
            brandService.save(brand)
            updateList()
            clearForm()
            
            Notification.show(
                if (existingBrand != null) "Marca actualizada" else "Marca creada",
                3000,
                Notification.Position.TOP_CENTER
            )
        } catch (e: Exception) {
            Notification.show("Error: ${e.message}", 3000, Notification.Position.TOP_CENTER)
        }
    }
    
    private fun toggleBrandStatus(brand: Brand, newStatus: Boolean) {
        try {
            if (newStatus) {
                brandService.activate(brand.id)
                Notification.show("Marca activada", 2000, Notification.Position.TOP_CENTER)
            } else {
                brandService.deactivate(brand.id)
                Notification.show("Marca desactivada", 2000, Notification.Position.TOP_CENTER)
            }
            updateList()
        } catch (e: Exception) {
            Notification.show("Error: ${e.message}", 3000, Notification.Position.TOP_CENTER)
        }
    }
    
    private fun clearForm() {
        nameField.clear()
        descriptionField.clear()
        activeField.value = true
    }
    
    private fun updateList() {
        grid.setItems(brandService.findAll())
    }
}