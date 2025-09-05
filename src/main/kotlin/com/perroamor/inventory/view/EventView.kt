package com.perroamor.inventory.view

import com.perroamor.inventory.entity.Event
import com.perroamor.inventory.service.EventService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import jakarta.annotation.security.RolesAllowed
import java.time.LocalDate
import java.util.*

@Route("events", layout = MainLayout::class)
@PageTitle("Eventos")
@RolesAllowed("ADMIN", "MANAGER")
class EventView(@Autowired private val eventService: EventService) : VerticalLayout() {
    
    private val grid = Grid(Event::class.java)
    private val nameField = TextField("Nombre del Evento")
    private val startDateField = DatePicker("Fecha de Inicio")
    private val endDateField = DatePicker("Fecha de Fin")
    private val locationField = TextField("Ubicación")
    private val descriptionField = TextArea("Descripción")
    private val isActiveCheckbox = Checkbox("Evento Activo")
    
    init {
        setSizeFull()
        configureGrid()
        configureForm()
        
        add(
            H2("Gestión de Eventos"),
            createToolbar(),
            grid
        )
        
        updateList()
    }
    
    private fun configureGrid() {
        grid.setSizeFull()
        grid.removeAllColumns()
        
        grid.addColumn(Event::id).setHeader("ID").setWidth("80px").setFlexGrow(0)
        grid.addColumn(Event::name).setHeader("Nombre").setWidth("200px").setFlexGrow(1)
        grid.addColumn(Event::startDate).setHeader("Inicio").setWidth("120px").setFlexGrow(0)
        grid.addColumn(Event::endDate).setHeader("Fin").setWidth("120px").setFlexGrow(0)
        grid.addColumn(Event::location).setHeader("Ubicación").setFlexGrow(1)
        
        grid.addColumn { event ->
            when {
                event.isCurrentlyActive() -> "🟢 En curso"
                event.startDate.isAfter(LocalDate.now()) -> "🔵 Próximo"
                else -> "🔴 Finalizado"
            }
        }.setHeader("Estado").setWidth("130px").setFlexGrow(0)
        
        grid.addColumn { event ->
            "${event.getDurationInDays()} días"
        }.setHeader("Duración").setWidth("100px").setFlexGrow(0)
        
        grid.addColumn { event ->
            if (event.isActive) "✅ Activo" else "❌ Inactivo"
        }.setHeader("Activo").setWidth("100px").setFlexGrow(0)
        
        // Columna de acciones para ventas
        grid.addComponentColumn { event ->
            val buttonsLayout = HorizontalLayout()
            
            val newSaleButton = Button(Icon(VaadinIcon.PLUS)) {
                navigateToNewSale(event)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY)
                element.setAttribute("title", "Nueva Venta")
            }
            
            val viewSalesButton = Button("Ver Ventas", Icon(VaadinIcon.LIST)) {
                navigateToSales(event)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY)
            }
            
            buttonsLayout.add(newSaleButton, viewSalesButton)
            buttonsLayout
        }.setHeader("Ventas").setWidth("220px").setFlexGrow(0)
        
        grid.asSingleSelect().addValueChangeListener { event ->
            event.value?.let { editEvent(it) }
        }
    }
    
    private fun configureForm() {
        startDateField.locale = Locale.of("es", "MX")
        endDateField.locale = Locale.of("es", "MX")
        
        // Configurar fecha mínima como hoy
        startDateField.min = LocalDate.now()
        endDateField.min = LocalDate.now()
        
        // Auto-actualizar fecha fin cuando cambie fecha inicio
        startDateField.addValueChangeListener { event ->
            event.value?.let { startDate ->
                endDateField.min = startDate
                if (endDateField.value != null && endDateField.value.isBefore(startDate)) {
                    endDateField.value = startDate
                }
            }
        }
        
        isActiveCheckbox.value = true
        descriptionField.height = "100px"
    }
    
    private fun createToolbar(): HorizontalLayout {
        val addButton = Button("Nuevo Evento") { addEvent() }
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        val refreshButton = Button("Actualizar") { updateList() }
        
        return HorizontalLayout(addButton, refreshButton)
    }
    
    private fun addEvent() {
        showEventDialog(null)
    }
    
    private fun editEvent(event: Event) {
        showEventDialog(event)
    }
    
    private fun showEventDialog(event: Event?) {
        val dialog = Dialog()
        val formLayout = FormLayout()
        
        if (event != null) {
            nameField.value = event.name
            startDateField.value = event.startDate
            endDateField.value = event.endDate
            locationField.value = event.location
            descriptionField.value = event.description ?: ""
            isActiveCheckbox.value = event.isActive
        } else {
            clearForm()
        }
        
        formLayout.add(
            nameField, startDateField, endDateField, 
            locationField, descriptionField, isActiveCheckbox
        )
        
        val saveButton = Button("Guardar") {
            saveEvent(event)
            dialog.close()
        }
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        val cancelButton = Button("Cancelar") { dialog.close() }
        
        val buttonLayout = HorizontalLayout(saveButton, cancelButton)
        
        dialog.add(
            VerticalLayout(
                if (event != null) H3("Editar Evento") else H3("Nuevo Evento"),
                formLayout,
                buttonLayout
            )
        )
        
        dialog.open()
    }
    
    private fun saveEvent(existingEvent: Event?) {
        try {
            val event = if (existingEvent != null) {
                existingEvent.copy(
                    name = nameField.value,
                    startDate = startDateField.value,
                    endDate = endDateField.value,
                    location = locationField.value,
                    description = if (descriptionField.value.isBlank()) null else descriptionField.value,
                    isActive = isActiveCheckbox.value
                )
            } else {
                Event(
                    name = nameField.value,
                    startDate = startDateField.value,
                    endDate = endDateField.value,
                    location = locationField.value,
                    description = if (descriptionField.value.isBlank()) null else descriptionField.value,
                    isActive = isActiveCheckbox.value
                )
            }
            
            eventService.save(event)
            updateList()
            clearForm()
            
            Notification.show(
                if (existingEvent != null) "Evento actualizado" else "Evento creado",
                3000,
                Notification.Position.TOP_CENTER
            )
        } catch (e: Exception) {
            Notification.show("Error: ${e.message}", 3000, Notification.Position.TOP_CENTER)
        }
    }
    
    private fun clearForm() {
        nameField.clear()
        startDateField.clear()
        endDateField.clear()
        locationField.clear()
        descriptionField.clear()
        isActiveCheckbox.value = true
    }
    
    private fun navigateToNewSale(event: Event) {
        // Navegar a la vista de nueva venta con el ID del evento como parámetro
        UI.getCurrent().navigate("new-sale/${event.id}")
    }
    
    private fun navigateToSales(event: Event) {
        // Navegar a la vista de ventas con el ID del evento como parámetro
        UI.getCurrent().navigate("sales/${event.id}")
    }
    
    private fun updateList() {
        grid.setItems(eventService.findAll())
    }
}