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
import com.vaadin.flow.component.textfield.BigDecimalField
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.*
import org.springframework.beans.factory.annotation.Autowired
import jakarta.annotation.security.RolesAllowed
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    private val dateFilter = ComboBox<LocalDate>("Filtrar por Fecha")
    private val brandFilter = ComboBox<String>("Filtrar por Marca")
    private val paymentMethodFilter = ComboBox<String>("Filtrar por Pago")
    private val totalSalesLabel = Span("Total mostrado: $0.00")
    private val grid = Grid(Sale::class.java)
    private val expandedSales = mutableSetOf<Long>()
    
    private var selectedEventId: Long? = null
    private var selectedDate: LocalDate? = null
    private var selectedBrand: String? = null
    private var selectedPaymentMethod: String? = null
    
    init {
        setSizeFull()
        setupEventSelector()
        setupDateFilter()
        setupBrandFilter()
        setupPaymentMethodFilter()
        setupTotalSalesLabel()
        configureGrid()
        
        add(
            H2("Gestión de Ventas"),
            createToolbar(),
            createFiltersLayout(),
            grid
        )
        
        updateEventList()
    }
    
    private fun setupEventSelector() {
        eventSelector.setItemLabelGenerator { "${it.name} (${it.location})" }
        eventSelector.element.style.set("font-size", "1.1em")
        eventSelector.element.style.set("min-height", "45px")
        
        eventSelector.addValueChangeListener { event ->
            event.value?.let { selectedEvent ->
                selectedEventId = selectedEvent.id
                updateDateFilterOptions(selectedEvent)
                updateSalesGrid(selectedEvent)
            } ?: run {
                selectedEventId = null
                selectedDate = null
                selectedBrand = null
                selectedPaymentMethod = null
                dateFilter.setItems(emptyList())
                dateFilter.value = null
                brandFilter.setItems(emptyList())
                brandFilter.value = null
                paymentMethodFilter.setItems(emptyList())
                paymentMethodFilter.value = null
                grid.setItems(emptyList())
                updateTotalSalesLabel(emptyList())
            }
        }
    }
    
    private fun setupDateFilter() {
        dateFilter.setItemLabelGenerator { date ->
            val dayOfWeek = date.dayOfWeek.getDisplayName(
                java.time.format.TextStyle.FULL, 
                Locale("es", "ES")
            ).uppercase()
            val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "ES"))
            "$dayOfWeek ${date.format(formatter)}"
        }
        dateFilter.element.style.set("font-size", "1.1em")
        dateFilter.element.style.set("min-height", "45px")
        dateFilter.placeholder = "Seleccionar día (opcional)"
        dateFilter.isClearButtonVisible = true
        
        dateFilter.addValueChangeListener { event ->
            selectedDate = event.value
            eventSelector.value?.let { selectedEvent ->
                updateSalesGrid(selectedEvent)
            }
        }
    }
    
    private fun setupBrandFilter() {
        brandFilter.element.style.set("font-size", "1.1em")
        brandFilter.element.style.set("min-height", "45px")
        brandFilter.placeholder = "Filtrar por marca (opcional)"
        brandFilter.isClearButtonVisible = true
        
        brandFilter.addValueChangeListener { event ->
            selectedBrand = event.value
            eventSelector.value?.let { selectedEvent ->
                updateSalesGrid(selectedEvent)
            }
        }
    }
    
    private fun setupPaymentMethodFilter() {
        paymentMethodFilter.element.style.set("font-size", "1.1em")
        paymentMethodFilter.element.style.set("min-height", "45px")
        paymentMethodFilter.placeholder = "Filtrar por pago (opcional)"
        paymentMethodFilter.isClearButtonVisible = true
        
        paymentMethodFilter.addValueChangeListener { event ->
            selectedPaymentMethod = event.value
            eventSelector.value?.let { selectedEvent ->
                updateSalesGrid(selectedEvent)
            }
        }
    }
    
    private fun setupTotalSalesLabel() {
        totalSalesLabel.element.style.set("font-weight", "bold")
        totalSalesLabel.element.style.set("font-size", "1.1em")
        totalSalesLabel.element.style.set("color", "var(--lumo-primary-text-color)")
        totalSalesLabel.element.style.set("background-color", "var(--lumo-contrast-5pct)")
        totalSalesLabel.element.style.set("padding", "8px 12px")
        totalSalesLabel.element.style.set("border-radius", "var(--lumo-border-radius-m)")
        totalSalesLabel.element.style.set("border", "1px solid var(--lumo-contrast-20pct)")
        totalSalesLabel.element.style.set("white-space", "nowrap")
    }
    
    private fun updateDateFilterOptions(event: Event) {
        val sales = saleService.findByEvent(event)
        val mexicoZone = ZoneId.of("America/Mexico_City")
        val uniqueDates = sales.map { sale ->
            val mexicoDateTime = sale.saleDate.atZone(ZoneId.systemDefault()).withZoneSameInstant(mexicoZone)
            mexicoDateTime.toLocalDate()
        }.distinct().sorted()
        
        dateFilter.setItems(uniqueDates)
        selectedDate = null
        dateFilter.value = null
        
        // Actualizar opciones de marca y método de pago
        updateBrandFilterOptions(event)
        updatePaymentMethodFilterOptions(event)
    }
    
    private fun updateBrandFilterOptions(event: Event) {
        val sales = saleService.findByEvent(event)
        val brands = mutableSetOf<String>()
        
        sales.forEach { sale ->
            try {
                val saleItems = saleService.findSaleItems(sale.id)
                saleItems.forEach { item ->
                    brands.add(item.product.brand)
                }
            } catch (e: Exception) {
                // Ignorar errores al obtener items
            }
        }
        
        val sortedBrands = brands.toList().sorted()
        brandFilter.setItems(sortedBrands)
        selectedBrand = null
        brandFilter.value = null
    }
    
    private fun updatePaymentMethodFilterOptions(event: Event) {
        val sales = saleService.findByEvent(event)
        val paymentMethods = sales.map { it.paymentMethod }.distinct().sorted()
        
        paymentMethodFilter.setItems(paymentMethods)
        selectedPaymentMethod = null
        paymentMethodFilter.value = null
    }
    
    private fun configureGrid() {
        grid.setSizeFull()
        grid.removeAllColumns()
        
        // Columna de expansión
        grid.addComponentColumn { sale ->
            val expandIcon = if (expandedSales.contains(sale.id)) {
                Icon(VaadinIcon.MINUS_CIRCLE)
            } else {
                Icon(VaadinIcon.PLUS_CIRCLE)
            }
            
            val expandButton = Button(expandIcon) {
                toggleSaleExpansion(sale)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE)
                element.setAttribute("title", "Ver/Ocultar detalles")
            }
            
            expandButton
        }.setHeader("").setWidth("50px").setFlexGrow(0)
        
        grid.addColumn(Sale::id).setHeader("ID").setWidth("60px").setFlexGrow(0)
        
        grid.addColumn { sale ->
            val mexicoZone = ZoneId.of("America/Mexico_City")
            val mexicoDateTime = sale.saleDate.atZone(ZoneId.systemDefault()).withZoneSameInstant(mexicoZone)
            "${mexicoDateTime.toLocalDate()} ${mexicoDateTime.toLocalTime().toString().substring(0,5)}"
        }.setHeader("Fecha y Hora").setWidth("200px").setFlexGrow(0)
        
        grid.addColumn { sale ->
            "$${sale.totalAmount}"
        }.setHeader("Total").setWidth("100px").setFlexGrow(0)
        
        grid.addColumn(Sale::paymentMethod).setHeader("Pago").setWidth("200px").setFlexGrow(0)
        
        grid.addColumn { sale ->
            getBrandsForSale(sale)
        }.setHeader("Marcas").setWidth("250px").setFlexGrow(0)
        
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
            
            buttonsLayout
        }.setHeader("Acciones").setFlexGrow(1)
        
        // Configurar la expansión de filas con detalles
        grid.setItemDetailsRenderer(ComponentRenderer { sale ->
            createSaleDetailsComponent(sale)
        })
        
        // Controlar qué filas están expandidas
        grid.setDetailsVisibleOnClick(false)
        grid.addItemClickListener { event ->
            if (event.clickCount == 1) {
                toggleSaleExpansion(event.item)
            }
        }
    }
    
    private fun createSaleDetailsComponent(sale: Sale): VerticalLayout {
        val detailsLayout = VerticalLayout()
        detailsLayout.isPadding = true
        detailsLayout.isSpacing = true
        detailsLayout.style.set("background-color", "var(--lumo-contrast-5pct)")
        detailsLayout.style.set("border-left", "3px solid var(--lumo-primary-color)")
        detailsLayout.style.set("margin", "8px 0")
        
        // Información adicional de la venta
        val saleInfoLayout = HorizontalLayout()
        saleInfoLayout.setWidthFull()
        saleInfoLayout.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        
        val leftInfo = VerticalLayout()
        leftInfo.isPadding = false
        leftInfo.isSpacing = false
        
        if (sale.customerName != null) {
            leftInfo.add(Span("👤 Cliente: ${sale.customerName}"))
        }
        if (sale.customerPhone != null) {
            leftInfo.add(Span("📞 Teléfono: ${sale.customerPhone}"))
        }
        
        val rightInfo = VerticalLayout()
        rightInfo.isPadding = false
        rightInfo.isSpacing = false
        rightInfo.add(Span("💳 Método: ${sale.paymentMethod}"))
        rightInfo.add(Span("💰 Total: $${sale.totalAmount}"))
        
        saleInfoLayout.add(leftInfo, rightInfo)
        detailsLayout.add(saleInfoLayout)
        
        // Tabla de productos
        val productsLabel = Span("🛍️ Productos:")
        productsLabel.style.set("font-weight", "bold")
        productsLabel.style.set("color", "var(--lumo-primary-text-color)")
        detailsLayout.add(productsLabel)
        
        val itemsGrid = Grid<SaleItem>()
        itemsGrid.height = "200px"
        itemsGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES)
        
        // Configurar columnas del grid de items
        itemsGrid.addColumn { item ->
            if (item.variant != null) {
                "${item.product.name} - ${item.variant!!.variantName}"
            } else {
                item.product.name
            }
        }.setHeader("Producto").setFlexGrow(2)
        
        itemsGrid.addColumn { "${it.quantity}" }.setHeader("Cant.").setWidth("80px").setFlexGrow(0)
        itemsGrid.addColumn { "$${it.unitPrice}" }.setHeader("P. Unit.").setWidth("100px").setFlexGrow(0)
        itemsGrid.addColumn { "$${it.totalPrice}" }.setHeader("Total").setWidth("100px").setFlexGrow(0)
        
        itemsGrid.addColumn { item ->
            item.personalization ?: "-"
        }.setHeader("Personalización").setFlexGrow(1)
        
        // Cargar items de la venta
        val saleItems = saleService.findSaleItems(sale.id)
        itemsGrid.setItems(saleItems)
        
        detailsLayout.add(itemsGrid)
        
        // Resumen del total calculado
        val calculatedTotal = saleItems.sumOf { it.totalPrice }
        val totalLayout = HorizontalLayout()
        totalLayout.setWidthFull()
        totalLayout.justifyContentMode = FlexComponent.JustifyContentMode.END
        
        val totalLabel = Span("Total calculado: $$calculatedTotal")
        totalLabel.style.set("font-weight", "bold")
        totalLabel.style.set("font-size", "1.1em")
        totalLabel.style.set("color", "var(--lumo-primary-text-color)")
        
        totalLayout.add(totalLabel)
        detailsLayout.add(totalLayout)
        
        return detailsLayout
    }
    
    private fun createToolbar(): HorizontalLayout {
        val refreshButton = Button("Actualizar") { 
            eventSelector.value?.let { selectedEvent ->
                updateDateFilterOptions(selectedEvent)
                updateSalesGrid(selectedEvent)
            }
        }
        
        val statisticsButton = Button("Estadísticas", Icon(VaadinIcon.CHART)) {
            eventSelector.value?.let { event -> 
                eventStatisticsDialog.show(event)
            }
        }
        
        return HorizontalLayout(refreshButton, statisticsButton)
    }
    
    private fun createFiltersLayout(): HorizontalLayout {
        val filtersLayout = HorizontalLayout()
        filtersLayout.setWidthFull()
        filtersLayout.isSpacing = true
        filtersLayout.setAlignItems(FlexComponent.Alignment.END)
        filtersLayout.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        
        // Layout izquierdo con los filtros
        val leftFiltersLayout = HorizontalLayout()
        leftFiltersLayout.isSpacing = true
        leftFiltersLayout.setAlignItems(FlexComponent.Alignment.END)
        
        // Configurar el eventSelector para que se vea mejor en el layout horizontal
        eventSelector.setWidth("400px")
        
        // Configurar el dateFilter para que se vea mejor en el layout horizontal  
        dateFilter.setWidth("250px")
        
        // Configurar el brandFilter
        brandFilter.setWidth("200px")
        
        // Configurar el paymentMethodFilter
        paymentMethodFilter.setWidth("180px")
        
        leftFiltersLayout.add(eventSelector, dateFilter, brandFilter, paymentMethodFilter)
        
        // Agregar margen inferior al layout de filtros
        filtersLayout.element.style.set("margin-bottom", "16px")
        
        filtersLayout.add(leftFiltersLayout, totalSalesLabel)
        
        return filtersLayout
    }
    
    
    private fun getBrandsForSale(sale: Sale): String {
        return try {
            val saleItems = saleService.findSaleItems(sale.id)
            val brands = saleItems.map { it.product.brand }.distinct().sorted()
            if (brands.isNotEmpty()) {
                brands.joinToString(", ")
            } else {
                "-"
            }
        } catch (e: Exception) {
            "-"
        }
    }
    
    private fun toggleSaleExpansion(sale: Sale) {
        val saleId = sale.id
        
        if (expandedSales.contains(saleId)) {
            // Contraer la fila
            expandedSales.remove(saleId)
            grid.setDetailsVisible(sale, false)
        } else {
            // Expandir la fila
            expandedSales.add(saleId)
            grid.setDetailsVisible(sale, true)
        }
        
        // Actualizar el icono del botón de expansión
        grid.dataProvider.refreshItem(sale)
    }
    
    private fun editSale(sale: Sale) {
        if (sale.isCancelled) {
            Notification.show(
                "No se puede editar una venta cancelada",
                3000,
                Notification.Position.TOP_CENTER
            )
            return
        }
        
        val dialog = Dialog()
        dialog.width = "500px"
        dialog.isCloseOnEsc = true
        dialog.isCloseOnOutsideClick = false
        
        val content = VerticalLayout()
        content.isPadding = true
        content.isSpacing = true
        
        // Título
        content.add(H2("✏️ Editar Venta #${sale.id}"))
        
        // Campo para editar el total
        val totalField = BigDecimalField("Total de la Venta")
        totalField.value = sale.totalAmount
        totalField.prefixComponent = Span("$")
        totalField.setWidthFull()
        totalField.placeholder = "0.00"
        
        // ComboBox para método de pago
        val paymentMethodCombo = ComboBox<String>("Método de Pago")
        paymentMethodCombo.setItems(
            "Efectivo",
            "Tarjeta",
            "Transferencia",
            "Depósito",
            "PayPal",
            "Mercado Pago",
            "Otro"
        )
        paymentMethodCombo.value = sale.paymentMethod
        paymentMethodCombo.setWidthFull()
        
        content.add(totalField, paymentMethodCombo)
        
        // Botones
        val buttonsLayout = HorizontalLayout()
        buttonsLayout.setWidthFull()
        buttonsLayout.justifyContentMode = FlexComponent.JustifyContentMode.END
        
        val cancelButton = Button("Cancelar") { dialog.close() }
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        
        val saveButton = Button("Guardar Cambios", Icon(VaadinIcon.CHECK)) {
            saveSaleChanges(sale, totalField.value, paymentMethodCombo.value, dialog)
        }
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        buttonsLayout.add(cancelButton, saveButton)
        content.add(buttonsLayout)
        
        dialog.add(content)
        dialog.open()
    }
    
    private fun saveSaleChanges(sale: Sale, newTotal: BigDecimal?, newPaymentMethod: String?, dialog: Dialog) {
        if (newTotal == null || newTotal <= BigDecimal.ZERO) {
            Notification.show(
                "El total debe ser mayor a cero",
                3000,
                Notification.Position.TOP_CENTER
            )
            return
        }
        
        if (newPaymentMethod.isNullOrBlank()) {
            Notification.show(
                "Debe seleccionar un método de pago",
                3000,
                Notification.Position.TOP_CENTER
            )
            return
        }
        
        try {
            // Actualizar la venta usando copy() para data class
            val updatedSale = sale.copy(
                totalAmount = newTotal,
                paymentMethod = newPaymentMethod
            )
            
            saleService.save(updatedSale)
            
            // Actualizar la grid
            eventSelector.value?.let { updateSalesGrid(it) }
            
            dialog.close()
            
            Notification.show(
                "✅ Venta #${sale.id} actualizada correctamente",
                3000,
                Notification.Position.TOP_CENTER
            )
        } catch (e: Exception) {
            Notification.show(
                "Error al actualizar la venta: ${e.message}",
                5000,
                Notification.Position.TOP_CENTER
            )
        }
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
        val mexicoZone = ZoneId.of("America/Mexico_City")
        val mexicoDateTime = sale.saleDate.atZone(ZoneId.systemDefault()).withZoneSameInstant(mexicoZone)
        
        saleInfoLayout.add(
            createInfoLine("📅 Fecha:", "${mexicoDateTime.toLocalDate()} ${mexicoDateTime.toLocalTime()}"),
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
        val saleItems = saleService.findSaleItems(sale.id)
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
            updateDateFilterOptions(currentEvent)
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
        var sales = saleService.findByEvent(event)
        
        // Filtrar por fecha si hay una seleccionada
        selectedDate?.let { date ->
            val mexicoZone = ZoneId.of("America/Mexico_City")
            sales = sales.filter { sale ->
                val mexicoDateTime = sale.saleDate.atZone(ZoneId.systemDefault()).withZoneSameInstant(mexicoZone)
                mexicoDateTime.toLocalDate() == date
            }
        }
        
        // Filtrar por marca si hay una seleccionada
        selectedBrand?.let { brand ->
            sales = sales.filter { sale ->
                try {
                    val saleItems = saleService.findSaleItems(sale.id)
                    saleItems.any { item -> item.product.brand == brand }
                } catch (e: Exception) {
                    false
                }
            }
        }
        
        // Filtrar por método de pago si hay uno seleccionado
        selectedPaymentMethod?.let { paymentMethod ->
            sales = sales.filter { sale ->
                sale.paymentMethod == paymentMethod
            }
        }
        
        val sortedSales = sales.sortedBy { it.id }
        grid.setItems(sortedSales)
        
        // Actualizar el total mostrado
        updateTotalSalesLabel(sortedSales)
    }
    
    private fun updateTotalSalesLabel(sales: List<Sale>) {
        val total = sales.filter { !it.isCancelled }.sumOf { it.totalAmount }
        val salesCount = sales.filter { !it.isCancelled }.size
        val cancelledCount = sales.count { it.isCancelled }
        
        val totalText = if (cancelledCount > 0) {
            "Total mostrado: $$total ($salesCount ventas${if (cancelledCount > 0) " + $cancelledCount canceladas" else ""})"
        } else {
            "Total mostrado: $$total ($salesCount ventas)"
        }
        
        totalSalesLabel.text = totalText
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
                    updateDateFilterOptions(foundEvent)
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