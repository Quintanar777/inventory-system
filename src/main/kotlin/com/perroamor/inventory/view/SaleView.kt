package com.perroamor.inventory.view

import com.perroamor.inventory.entity.Sale
import com.perroamor.inventory.entity.SaleItem
import com.perroamor.inventory.entity.Event
import com.perroamor.inventory.service.SaleService
import com.perroamor.inventory.service.EventService
import com.perroamor.inventory.view.component.EventStatisticsDialog
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.router.*
import org.springframework.beans.factory.annotation.Autowired
import jakarta.annotation.security.RolesAllowed
import java.math.BigDecimal

@Route("sales", layout = MainLayout::class)
@RouteAlias("sales/:eventId", layout = MainLayout::class)
@PageTitle("Ventas")
@RolesAllowed("ADMIN", "MANAGER", "EMPLOYEE")
class SaleView(
    @Autowired private val saleService: SaleService,
    @Autowired private val eventService: EventService,
    @Autowired private val eventStatisticsDialog: EventStatisticsDialog
) : VerticalLayout(), BeforeEnterObserver {
    
    private val eventSelector = ComboBox<Event>("Evento")
    private val grid = Grid(Sale::class.java)
    
    
    private var selectedEventId: Long? = null
    
    init {
        setSizeFull()
        setupEventSelector()
        configureGrid()
        
        add(
            H2("Gestión de Ventas"),
            createToolbar(),
            eventSelector,
            grid
        )
        
        updateEventList()
    }
    
    private fun setupEventSelector() {
        eventSelector.setItemLabelGenerator { "${it.name} (${it.location})" }
        eventSelector.setWidthFull()
        eventSelector.element.style.set("font-size", "1.1em")
        eventSelector.element.style.set("min-height", "45px")
        eventSelector.element.style.set("padding-top", "8px")
        eventSelector.element.style.set("margin-bottom", "16px")
        
        eventSelector.addValueChangeListener { event ->
            event.value?.let { selectedEvent ->
                selectedEventId = selectedEvent.id
                updateSalesGrid(selectedEvent)
            } ?: run {
                selectedEventId = null
                grid.setItems(emptyList())
            }
        }
    }
    
    private fun configureGrid() {
        grid.setSizeFull()
        grid.removeAllColumns()
        
        grid.addColumn(Sale::id).setHeader("ID").setWidth("60px").setFlexGrow(0)
        
        grid.addColumn { sale ->
            "${sale.saleDate.toLocalDate()} ${sale.saleDate.toLocalTime().toString().substring(0,5)}"
        }.setHeader("Fecha y Hora").setWidth("200px").setFlexGrow(0)
        
        grid.addColumn { sale ->
            "$${sale.totalAmount}"
        }.setHeader("Total").setWidth("100px").setFlexGrow(0)
        
        grid.addColumn(Sale::paymentMethod).setHeader("Pago").setWidth("200px").setFlexGrow(0)
        
        grid.addColumn { sale ->
            when {
                sale.customerName != null -> sale.customerName!!
                sale.customerPhone != null -> sale.customerPhone!!
                else -> "Sin datos"
            }
        }.setHeader("Cliente").setWidth("250px").setFlexGrow(0)
        
        grid.addColumn { sale ->
            when {
                sale.isCancelled -> "❌ Cancelada"
                sale.isPaid -> "✅ Pagada"
                else -> "⏳ Pendiente"
            }
        }.setHeader("Estado").setWidth("120px").setFlexGrow(0)
        
        // Columna de acciones
        grid.addComponentColumn { sale ->
            val buttonsLayout = HorizontalLayout()
            
            val detailsButton = Button("Detalles", Icon(VaadinIcon.EYE)) {
                showSaleDetails(sale)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY)
            }
            
            if (!sale.isCancelled) {
                val editButton = Button("Editar", Icon(VaadinIcon.EDIT)) {
                    editSale(sale)
                }.apply {
                    addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY)
                }
                buttonsLayout.add(editButton)
                
                val deleteButton = Button(Icon(VaadinIcon.TRASH)) {
                    confirmDeleteSale(sale)
                }.apply {
                    addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR)
                    element.setAttribute("title", "Eliminar Venta")
                }
                buttonsLayout.add(deleteButton)
            }
            
            buttonsLayout.add(detailsButton)
            buttonsLayout
        }.setHeader("Acciones").setFlexGrow(1)
        
        grid.asSingleSelect().addValueChangeListener { event ->
            event.value?.let { editSale(it) }
        }
    }
    
    private fun createToolbar(): HorizontalLayout {
        val refreshButton = Button("Actualizar") { 
            eventSelector.value?.let { updateSalesGrid(it) }
        }
        
        val statisticsButton = Button("Estadísticas", Icon(VaadinIcon.CHART)) {
            eventSelector.value?.let { event -> 
                eventStatisticsDialog.show(event)
            }
        }
        
        return HorizontalLayout(refreshButton, statisticsButton)
    }
    
    private fun editSale(sale: Sale) {
        // TODO: Implementar edición de venta existente
        Notification.show(
            "Próximamente: Edición de venta #${sale.id}",
            3000,
            Notification.Position.TOP_CENTER
        )
    }
    
    
    private fun showSaleDetails(sale: Sale) {
        val dialog = Dialog()
        dialog.width = "800px"
        dialog.height = "600px"
        
        val content = VerticalLayout()
        
        // Título con información de la venta
        content.add(H2("📄 Detalles de la Venta #${sale.id}"))
        
        // Información general de la venta
        val saleInfoLayout = VerticalLayout()
        saleInfoLayout.add(
            createInfoLine("📅 Fecha:", "${sale.saleDate.toLocalDate()} ${sale.saleDate.toLocalTime()}"),
            createInfoLine("💳 Método de pago:", sale.paymentMethod),
            createInfoLine("💰 Total:", "$${sale.totalAmount}")
        )
        
        if (sale.customerName != null) {
            saleInfoLayout.add(createInfoLine("👤 Cliente:", sale.customerName!!))
        }
        
        if (sale.customerPhone != null) {
            saleInfoLayout.add(createInfoLine("📞 Teléfono:", sale.customerPhone!!))
        }
        
        content.add(saleInfoLayout)
        
        // Grid de items vendidos
        content.add(H3("🛍️ Productos Vendidos"))
        
        val itemsGrid = Grid<SaleItem>()
        itemsGrid.height = "300px"
        itemsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        
        // Configurar columnas del grid de items
        itemsGrid.addColumn { item ->
            if (item.variant != null) {
                "${item.product.name} - ${item.variant!!.variantName}"
            } else {
                item.product.name
            }
        }.setHeader("Producto").setFlexGrow(2)
        
        itemsGrid.addColumn { "${it.quantity}" }.setHeader("Cantidad").setWidth("100px").setFlexGrow(0)
        itemsGrid.addColumn { "$${it.unitPrice}" }.setHeader("Precio Unit.").setWidth("120px").setFlexGrow(0)
        itemsGrid.addColumn { "$${it.totalPrice}" }.setHeader("Total").setWidth("120px").setFlexGrow(0)
        
        itemsGrid.addColumn { item ->
            item.personalization ?: "-"
        }.setHeader("Personalización").setFlexGrow(1)
        
        // Cargar items de la venta
        val saleItems = saleService.findSaleItems(sale.id!!)
        itemsGrid.setItems(saleItems)
        
        content.add(itemsGrid)
        
        // Mostrar total calculado
        val calculatedTotal = saleItems.sumOf { it.totalPrice }
        val totalSummary = HorizontalLayout()
        val totalLabel = Span("TOTAL CALCULADO: $$calculatedTotal")
        totalLabel.style.set("font-weight", "bold")
        totalLabel.style.set("font-size", "1.2em")
        totalLabel.style.set("color", "var(--lumo-primary-text-color)")
        
        totalSummary.add(totalLabel)
        totalSummary.justifyContentMode = FlexComponent.JustifyContentMode.END
        totalSummary.setWidthFull()
        
        content.add(totalSummary)
        
        // Botón cerrar
        val closeButton = Button("Cerrar") { dialog.close() }
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        content.add(HorizontalLayout(closeButton).apply {
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        })
        
        dialog.add(content)
        dialog.open()
    }
    
    private fun createInfoLine(label: String, value: String): HorizontalLayout {
        val layout = HorizontalLayout()
        
        val labelSpan = Span(label)
        labelSpan.style.set("font-weight", "bold")
        labelSpan.style.set("color", "var(--lumo-secondary-text-color)")
        
        val valueSpan = Span(value)
        valueSpan.style.set("color", "var(--lumo-primary-text-color)")
        
        layout.add(labelSpan, valueSpan)
        layout.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        layout.setWidthFull()
        
        return layout
    }
    
    
    private fun updateEventList() {
        val currentEvents = eventService.findCurrentEvents()
        val upcomingEvents = eventService.findUpcomingEvents()
        val allActiveEvents = (currentEvents + upcomingEvents).distinctBy { it.id }
        
        eventSelector.setItems(allActiveEvents)
        
        // Auto-seleccionar evento en curso si existe y no hay uno ya seleccionado por URL
        if (selectedEventId == null && currentEvents.isNotEmpty()) {
            val currentEvent = currentEvents.first() // Tomar el primer evento en curso
            eventSelector.value = currentEvent
            selectedEventId = currentEvent.id
            updateSalesGrid(currentEvent)
            
            Notification.show(
                "✅ Evento en curso seleccionado: ${currentEvent.name}",
                3000,
                Notification.Position.TOP_CENTER
            )
        }
    }
    
    private fun confirmDeleteSale(sale: Sale) {
        val confirmDialog = ConfirmDialog(
            "Confirmar Eliminación",
            "¿Estás seguro de que deseas eliminar la venta #${sale.id}? Esta acción no se puede deshacer.",
            "Sí, Eliminar",
            { deleteSale(sale) },
            "Cancelar",
            { /* No hacer nada al cancelar */ }
        )
        
        confirmDialog.setConfirmButtonTheme("error primary")
        confirmDialog.open()
    }
    
    private fun deleteSale(sale: Sale) {
        try {
            saleService.delete(sale.id)
            
            // Actualizar la grid después de eliminar
            eventSelector.value?.let { updateSalesGrid(it) }
            
            Notification.show(
                "Venta #${sale.id} eliminada correctamente",
                3000,
                Notification.Position.TOP_CENTER
            )
        } catch (e: Exception) {
            Notification.show(
                "Error al eliminar la venta: ${e.message}",
                5000,
                Notification.Position.TOP_CENTER
            )
        }
    }
    
    private fun updateSalesGrid(event: Event) {
        val sales = saleService.findByEvent(event)
        grid.setItems(sales)
    }
    
    override fun beforeEnter(event: BeforeEnterEvent) {
        val eventIdParam = event.routeParameters.get("eventId")
        if (eventIdParam.isPresent) {
            val eventIdStr = eventIdParam.get()
            try {
                val eventId = eventIdStr.toLong()
                val foundEvent = eventService.findById(eventId)
                if (foundEvent != null) {
                    eventSelector.value = foundEvent
                    selectedEventId = eventId
                    updateSalesGrid(foundEvent)
                    Notification.show(
                        "Mostrando ventas de: ${foundEvent.name}",
                        3000,
                        Notification.Position.TOP_CENTER
                    )
                } else {
                    Notification.show(
                        "Evento no encontrado",
                        3000,
                        Notification.Position.MIDDLE
                    )
                }
            } catch (e: NumberFormatException) {
                Notification.show(
                    "ID de evento inválido",
                    3000,
                    Notification.Position.MIDDLE
                )
            }
        }
    }
}