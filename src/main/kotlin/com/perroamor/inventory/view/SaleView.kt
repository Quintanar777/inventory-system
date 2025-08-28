package com.perroamor.inventory.view

import com.perroamor.inventory.entity.Sale
import com.perroamor.inventory.entity.SaleItem
import com.perroamor.inventory.entity.Event
import com.perroamor.inventory.service.SaleService
import com.perroamor.inventory.service.EventService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
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
import java.math.BigDecimal

@Route("sales", layout = MainLayout::class)
@RouteAlias("sales/:eventId", layout = MainLayout::class)
@PageTitle("Ventas")
class SaleView(
    @Autowired private val saleService: SaleService,
    @Autowired private val eventService: EventService
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
            sale.saleDate.toLocalDate().toString()
        }.setHeader("Fecha").setWidth("110px").setFlexGrow(0)
        
        grid.addColumn { sale ->
            sale.saleDate.toLocalTime().toString()
        }.setHeader("Hora").setWidth("85px").setFlexGrow(0)
        
        grid.addColumn { sale ->
            "$${sale.totalAmount}"
        }.setHeader("Total").setWidth("100px").setFlexGrow(0)
        
        grid.addColumn(Sale::paymentMethod).setHeader("Pago").setFlexGrow(1)
        
        grid.addColumn { sale ->
            when {
                sale.isCancelled -> "❌ Cancelada"
                sale.isPaid -> "✅ Pagada"
                else -> "⏳ Pendiente"
            }
        }.setHeader("Estado").setFlexGrow(1)
        
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
            selectedEventId?.let { showEventStatistics(it) }
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
    
    private fun showEventStatistics(eventId: Long) {
        val event = eventService.findById(eventId)
        if (event == null) {
            Notification.show("Evento no encontrado", 3000, Notification.Position.TOP_CENTER)
            return
        }
        
        // Obtener estadísticas del evento
        val statistics = saleService.getEventStatistics(eventId)
        val totalSales = statistics["totalSales"] as Long
        val totalAmount = statistics["totalAmount"] as BigDecimal
        val paymentMethods = statistics["paymentMethods"] as List<Array<Any>>
        
        showStatisticsDialog(event, totalSales, totalAmount, paymentMethods)
    }
    
    private fun showStatisticsDialog(
        event: Event, 
        totalSales: Long, 
        totalAmount: BigDecimal, 
        paymentMethods: List<Array<Any>>
    ) {
        val dialog = Dialog()
        dialog.width = "500px"
        
        val content = VerticalLayout()
        
        // Título
        content.add(H2("📊 Estadísticas del Evento"))
        content.add(H3(event.name))
        
        // Estadísticas generales
        val generalStats = VerticalLayout()
        generalStats.add(
            createStatLine("🛒 Total de Ventas:", "$totalSales ventas"),
            createStatLine("💰 Ingresos Totales:", "$$totalAmount")
        )
        content.add(generalStats)
        
        // Estadísticas por método de pago
        if (paymentMethods.isNotEmpty()) {
            content.add(H3("💳 Ventas por Método de Pago"))
            
            val paymentStats = VerticalLayout()
            paymentMethods.forEach { methodData ->
                val paymentMethod = methodData[0] as String
                val count = methodData[1] as Long
                val amount = methodData[2] as BigDecimal
                
                val icon = when (paymentMethod.lowercase()) {
                    "efectivo" -> "💵"
                    "tarjeta de débito", "tarjeta de crédito" -> "💳"
                    "transferencia" -> "🏦"
                    "paypal", "mercado pago" -> "📱"
                    else -> "💰"
                }
                
                paymentStats.add(
                    createPaymentStatLine(
                        "$icon $paymentMethod:", 
                        "$count ventas", 
                        "$$amount"
                    )
                )
            }
            content.add(paymentStats)
        } else {
            content.add(Span("Sin ventas registradas para este evento"))
        }
        
        // Botón cerrar
        val closeButton = Button("Cerrar") { dialog.close() }
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        content.add(HorizontalLayout(closeButton).apply {
            justifyContentMode = com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER
        })
        
        dialog.add(content)
        dialog.open()
    }
    
    private fun createStatLine(label: String, value: String): HorizontalLayout {
        val layout = HorizontalLayout()
        
        val labelSpan = Span(label)
        labelSpan.style.set("font-weight", "bold")
        
        val valueSpan = Span(value)
        valueSpan.style.set("color", "var(--lumo-primary-text-color)")
        valueSpan.style.set("font-weight", "bold")
        
        layout.add(labelSpan, valueSpan)
        layout.justifyContentMode = com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN
        layout.setWidthFull()
        
        return layout
    }
    
    private fun createPaymentStatLine(method: String, count: String, amount: String): HorizontalLayout {
        val layout = HorizontalLayout()
        
        val methodSpan = Span(method)
        methodSpan.style.set("font-weight", "500")
        
        val detailsLayout = HorizontalLayout()
        
        val countSpan = Span(count)
        countSpan.style.set("color", "var(--lumo-secondary-text-color)")
        countSpan.style.set("margin-right", "10px")
        
        val amountSpan = Span(amount)
        amountSpan.style.set("color", "var(--lumo-primary-text-color)")
        amountSpan.style.set("font-weight", "bold")
        
        detailsLayout.add(countSpan, amountSpan)
        
        layout.add(methodSpan, detailsLayout)
        layout.justifyContentMode = com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN
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