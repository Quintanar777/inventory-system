package com.perroamor.inventory.view

import com.perroamor.inventory.entity.Event
import com.perroamor.inventory.entity.Sale
import com.perroamor.inventory.entity.SaleItem
import com.perroamor.inventory.service.EventService
import com.perroamor.inventory.service.SaleService
import com.perroamor.inventory.service.ProductService
import com.perroamor.inventory.view.component.SaleItemDialog
import com.perroamor.inventory.view.component.ProductSearchMobile
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datetimepicker.DateTimePicker
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.router.*
import org.springframework.beans.factory.annotation.Autowired
import jakarta.annotation.security.RolesAllowed
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Route("new-sale", layout = MainLayout::class)
@RouteAlias("new-sale/:eventId", layout = MainLayout::class)
@PageTitle("Nueva Venta")
@RolesAllowed("ADMIN", "MANAGER", "EMPLOYEE")
class NewSaleView(
    @Autowired private val eventService: EventService,
    @Autowired private val saleService: SaleService,
    @Autowired private val productService: ProductService,
    @Autowired private val saleItemDialog: SaleItemDialog,
    @Autowired private val productSearchMobile: ProductSearchMobile
) : VerticalLayout(), BeforeEnterObserver {
    
    private val eventSelector = ComboBox<Event>("Evento")
    private val saleDateField = DateTimePicker("Fecha de Venta")
    
    private val itemsGrid = Grid<SaleItemDialog.SaleItemData>()
    private val totalLabel = Span("TOTAL: $0.00")
    
    private val saleItems = mutableListOf<SaleItemDialog.SaleItemData>()
    
    private val paymentMethods = listOf(
        "Efectivo", "Tarjeta", "Transferencia", "PayPal", "Mercado Pago"
    )
    
    private var selectedEventId: Long? = null
    private var lastWholesaleMode: Boolean = false

    // Customer information fields
    private var customerName: String? = null
    private var customerPhone: String? = null
    private var customerInfoLabel: Span? = null

    // Event section collapse state
    private var isEventSectionCollapsed: Boolean = false
    private var eventSectionContent: VerticalLayout? = null
    private var paymentSectionContent: VerticalLayout? = null
    private var collapseButton: Button? = null
    private var contentLayout: HorizontalLayout? = null
    
    init {
        setSizeFull()
        setupEventSelector()
        configureForm()
        configureItemsGrid()
        
        add(
            createEventAndPaymentSection(),
            createItemsSection(),
            createActionButtons()
        )
        
        updateEventList()
    }
    
    private fun setupEventSelector() {
        eventSelector.setItemLabelGenerator { event ->
            val status = when {
                event.isCurrentlyActive() -> "🟢 EN CURSO"
                event.startDate.isAfter(java.time.LocalDate.now()) -> "🔵 PRÓXIMO"
                else -> "🔴 FINALIZADO"
            }
            "${event.name} ($status)"
        }
        
        eventSelector.addValueChangeListener { event ->
            event.value?.let { selectedEvent ->
                selectedEventId = selectedEvent.id
                // Configurar fecha de venta dentro del rango del evento
                val now = LocalDateTime.now(ZoneId.of("America/Mexico_City"))
                val eventStart = selectedEvent.startDate.atStartOfDay()
                val eventEnd = selectedEvent.endDate.atTime(23, 59, 59)
                
                if (now.isBefore(eventStart)) {
                    saleDateField.value = eventStart
                } else if (now.isAfter(eventEnd)) {
                    saleDateField.value = eventEnd
                } else {
                    saleDateField.value = now
                }
            }
        }
    }
    
    private fun configureForm() {
        saleDateField.locale = Locale.of("es", "MX")
        saleDateField.value = LocalDateTime.now(ZoneId.of("America/Mexico_City"))
        
    }
    
    private fun configureItemsGrid() {
        itemsGrid.removeAllColumns()
        
        itemsGrid.addColumn { it.getDisplayName() }
            .setHeader("Producto").setFlexGrow(2)
        
        itemsGrid.addColumn { it.quantity }
            .setHeader("Cant.").setWidth("80px").setFlexGrow(0)
        
        itemsGrid.addColumn { "$${it.unitPrice}" }
            .setHeader("Precio Unit.").setWidth("120px").setFlexGrow(0)
        
        itemsGrid.addColumn { "$${it.getTotalPrice()}" }
            .setHeader("Total").setWidth("120px").setFlexGrow(0)
        
        itemsGrid.addComponentColumn { item ->
            val removeButton = Button(Icon(VaadinIcon.TRASH)) {
                removeItem(item)
            }
            removeButton.addThemeVariants(
                ButtonVariant.LUMO_SMALL, 
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY
            )
            removeButton
        }.setHeader("").setWidth("60px").setFlexGrow(0)
        
        itemsGrid.height = "300px"
        itemsGrid.element.style.set("font-size", "1.1em")
    }
    
    private fun createEventAndPaymentSection(): VerticalLayout {
        val mainLayout = VerticalLayout()
        mainLayout.setWidthFull()
        mainLayout.isSpacing = false
        mainLayout.isPadding = false

        // Header con botón de colapso
        val headerLayout = HorizontalLayout()
        headerLayout.setWidthFull()
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN)
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER)

        val headerTitle = H3("📅 Información del Evento y Venta")

        collapseButton = Button(Icon(VaadinIcon.ANGLE_UP)) {
            toggleEventSection()
        }
        collapseButton!!.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL)
        collapseButton!!.element.setAttribute("title", "Contraer/Expandir sección")

        headerLayout.add(headerTitle, collapseButton)

        // Contenido colapsable (toda la fila)
        contentLayout = HorizontalLayout()
        contentLayout!!.setWidthFull()

        // Sección del evento
        eventSectionContent = VerticalLayout()
        eventSectionContent!!.add(H3("📅 Evento"))
        eventSectionContent!!.add(eventSelector)

        // Sección de la venta
        paymentSectionContent = VerticalLayout()
        paymentSectionContent!!.add(H3("📅 Venta"))

        val paymentLayout = HorizontalLayout()
        paymentLayout.add(saleDateField)
        paymentLayout.setWidthFull()

        paymentSectionContent!!.add(paymentLayout)

        // Agregar ambas secciones al layout horizontal
        contentLayout!!.add(eventSectionContent, paymentSectionContent)
        contentLayout!!.setFlexGrow(1.0, eventSectionContent, paymentSectionContent)

        mainLayout.add(headerLayout, contentLayout)

        return mainLayout
    }

    private fun toggleEventSection() {
        isEventSectionCollapsed = !isEventSectionCollapsed

        // Ocultar/mostrar toda la fila de contenido (evento + venta)
        contentLayout?.isVisible = !isEventSectionCollapsed

        // Cambiar el ícono del botón
        if (isEventSectionCollapsed) {
            collapseButton?.icon = Icon(VaadinIcon.ANGLE_DOWN)
            // Aumentar altura del grid cuando la sección está contraída
            itemsGrid.height = "450px"
        } else {
            collapseButton?.icon = Icon(VaadinIcon.ANGLE_UP)
            // Altura normal del grid
            itemsGrid.height = "300px"
        }

        val action = if (isEventSectionCollapsed) "contraída" else "expandida"
        Notification.show(
            "Sección $action",
            1000,
            Notification.Position.TOP_CENTER
        )
    }

    private fun createItemsSection(): VerticalLayout {
        val layout = VerticalLayout()
        
        val headerLayout = HorizontalLayout()
        headerLayout.add(H3("🛍️ Productos de la Venta"))
        headerLayout.setWidthFull()
        
        // Ocultar el botón "Agregar Producto" - se comenta para mantener funcionalidad disponible pero no visible
        // val addButton = Button("Agregar Producto", Icon(VaadinIcon.PLUS)) {
        //     addProduct()
        // }
        // addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)

        // Usar un botón normal con un submenu simulado en lugar de MenuBar
        val searchButton = Button("🔍 Buscar (iPad)", Icon(VaadinIcon.MOBILE)) {
            showSearchOptionsDialog()
        }
        searchButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS)
        searchButton.element.style.set("margin-left", "8px")
        searchButton.element.style.set("height", "50px")
        searchButton.element.style.set("font-size", "1.2em")
        searchButton.element.style.set("padding", "0 20px")
        searchButton.element.style.set("min-width", "150px")


        val addMoreButton = Button("Agregar Más", Icon(VaadinIcon.CART)) {
            addMoreProducts()
        }
        addMoreButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS)
        addMoreButton.element.style.set("margin-left", "8px")
        addMoreButton.element.style.set("height", "50px")
        addMoreButton.element.style.set("font-size", "1.2em")
        addMoreButton.element.style.set("padding", "0 20px")
        addMoreButton.element.style.set("min-width", "150px")

        val saveButton = Button("💾 Guardar Venta", Icon(VaadinIcon.CHECK)) {
            saveSale()
        }
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS)
        saveButton.element.style.set("margin-left", "8px")
        saveButton.element.style.set("height", "50px")
        saveButton.element.style.set("font-size", "1.2em")
        saveButton.element.style.set("padding", "0 20px")
        saveButton.element.style.set("min-width", "180px")

        val buttonsLayout = HorizontalLayout(searchButton, addMoreButton, saveButton)
        buttonsLayout.isSpacing = true
        
        headerLayout.add(buttonsLayout)
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN)
        headerLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, buttonsLayout)
        
        layout.add(headerLayout, itemsGrid, createCustomerInfoSection(), createTotalSection())
        return layout
    }
    
    private fun createCustomerInfoSection(): HorizontalLayout {
        val layout = HorizontalLayout()
        layout.setWidthFull()
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START)
        layout.setAlignItems(FlexComponent.Alignment.CENTER)
        layout.element.style.set("margin-top", "8px")
        
        val customerButton = Button("👤 Información del Cliente", Icon(VaadinIcon.USER)) {
            showCustomerDialog()
        }
        customerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        
        customerInfoLabel = Span()
        updateCustomerInfoLabel(customerInfoLabel!!)
        customerInfoLabel!!.element.style.set("margin-left", "16px")
        customerInfoLabel!!.element.style.set("color", "var(--lumo-secondary-text-color)")
        
        layout.add(customerButton, customerInfoLabel)
        
        return layout
    }
    
    private fun showCustomerDialog() {
        val dialog = Dialog()
        dialog.headerTitle = "Información del Cliente"
        
        val formLayout = FormLayout()
        
        val nameField = TextField("Nombre del Cliente")
        nameField.value = customerName ?: ""
        nameField.placeholder = "Nombre completo (opcional)"
        nameField.setWidthFull()
        
        val phoneField = TextField("Teléfono")
        phoneField.value = customerPhone ?: ""
        phoneField.placeholder = "Número de teléfono (opcional)"
        phoneField.setWidthFull()
        
        formLayout.add(nameField, phoneField)
        formLayout.setResponsiveSteps(FormLayout.ResponsiveStep("0", 1))
        
        val saveButton = Button("Guardar") {
            customerName = if (nameField.value.isBlank()) null else nameField.value
            customerPhone = if (phoneField.value.isBlank()) null else phoneField.value
            
            customerInfoLabel?.let { updateCustomerInfoLabel(it) }
            
            Notification.show(
                "Información del cliente actualizada",
                2000,
                Notification.Position.TOP_CENTER
            )
            
            dialog.close()
        }
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        val clearButton = Button("Limpiar") {
            nameField.clear()
            phoneField.clear()
        }
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        
        val cancelButton = Button("Cancelar") {
            dialog.close()
        }
        
        val buttonLayout = HorizontalLayout(clearButton, cancelButton, saveButton)
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN)
        
        val content = VerticalLayout(formLayout, buttonLayout)
        content.isSpacing = true
        content.setWidth("400px")
        
        dialog.add(content)
        dialog.open()
    }
    
    private fun updateCustomerInfoLabel(label: Span) {
        if (customerName != null || customerPhone != null) {
            val parts = mutableListOf<String>()
            customerName?.let { parts.add(it) }
            customerPhone?.let { parts.add(it) }
            label.text = "Cliente: ${parts.joinToString(" - ")}"
        } else {
            label.text = "Sin información del cliente"
        }
    }
    
    private fun createTotalSection(): HorizontalLayout {
        val layout = HorizontalLayout()
        totalLabel.style.set("font-size", "1.8em")
        totalLabel.style.set("font-weight", "bold")
        totalLabel.style.set("color", "var(--lumo-primary-text-color)")
        totalLabel.style.set("background-color", "var(--lumo-contrast-5pct)")
        totalLabel.style.set("padding", "10px 20px")
        totalLabel.style.set("border-radius", "8px")
        totalLabel.style.set("border", "2px solid var(--lumo-primary-color)")

        layout.add(totalLabel)
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END)
        layout.setWidthFull()
        layout.element.style.set("margin-top", "15px")
        layout.element.style.set("margin-bottom", "10px")

        return layout
    }
    
    private fun createActionButtons(): HorizontalLayout {
        val layout = HorizontalLayout()

        val clearButton = Button("Limpiar Todo", Icon(VaadinIcon.REFRESH)) {
            clearSale()
        }
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY)

        layout.add(clearButton)
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START)
        layout.setWidthFull()

        return layout
    }
    
    private fun addProduct() {
        if (selectedEventId == null) {
            Notification.show("Selecciona un evento primero", 3000, Notification.Position.TOP_CENTER)
            return
        }
        
        saleItemDialog.show { saleItemData ->
            saleItems.add(saleItemData)
            updateItemsGrid()
            updateTotal()
        }
    }

    private fun showSearchOptionsDialog() {
        val dialog = Dialog()
        dialog.headerTitle = "🔍 Opciones de Búsqueda"

        val content = VerticalLayout()
        content.setWidth("300px")
        content.isSpacing = true

        val menudeoButton = Button("💰 Precios Menudeo") {
            openMobileSearch(isWholesale = false)
            dialog.close()
        }
        menudeoButton.setWidthFull()
        menudeoButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS)
        menudeoButton.element.style.set("height", "45px")
        menudeoButton.element.style.set("font-size", "1.1em")

        val mayoreoButton = Button("🏪 Precios Mayoreo") {
            openMobileSearch(isWholesale = true)
            dialog.close()
        }
        mayoreoButton.setWidthFull()
        mayoreoButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS)
        mayoreoButton.element.style.set("height", "45px")
        mayoreoButton.element.style.set("font-size", "1.1em")

        val cancelButton = Button("Cancelar") {
            dialog.close()
        }
        cancelButton.setWidthFull()
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY)

        content.add(menudeoButton, mayoreoButton, cancelButton)
        dialog.add(content)
        dialog.open()
    }

    private fun openMobileSearch(isWholesale: Boolean) {
        if (selectedEventId == null) {
            Notification.show("Selecciona un evento primero", 3000, Notification.Position.TOP_CENTER)
            return
        }
        
        // Recordar la última configuración usada
        lastWholesaleMode = isWholesale
        
        val modeText = if (isWholesale) "mayoreo" else "menudeo"
        Notification.show("Búsqueda iniciada en modo $modeText", 2000, Notification.Position.TOP_CENTER)
        
        productSearchMobile.show(isWholesale) { selectionData ->
            // Crear un SaleItemData con los valores seleccionados en el diálogo móvil
            val saleItemData = SaleItemDialog.SaleItemData(
                product = selectionData.product,
                variant = null, // Por ahora sin variantes
                quantity = selectionData.quantity,
                unitPrice = selectionData.unitPrice,
                personalization = null
            )
            
            saleItems.add(saleItemData)
            updateItemsGrid()
            updateTotal()
        }
    }
    
    private fun addMoreProducts() {
        if (selectedEventId == null) {
            Notification.show("Selecciona un evento primero", 3000, Notification.Position.TOP_CENTER)
            return
        }
        
        val modeText = if (lastWholesaleMode) "mayoreo" else "menudeo"
        Notification.show("Agregando más productos en modo $modeText", 2000, Notification.Position.TOP_CENTER)
        
        productSearchMobile.show(lastWholesaleMode) { selectionData ->
            // Crear un SaleItemData con los valores seleccionados
            val saleItemData = SaleItemDialog.SaleItemData(
                product = selectionData.product,
                variant = null,
                quantity = selectionData.quantity,
                unitPrice = selectionData.unitPrice,
                personalization = null
            )
            
            saleItems.add(saleItemData)
            updateItemsGrid()
            updateTotal()
        }
    }
    
    private fun removeItem(item: SaleItemDialog.SaleItemData) {
        saleItems.remove(item)
        updateItemsGrid()
        updateTotal()
        
        Notification.show(
            "Producto removido: ${item.getDisplayName()}",
            2000,
            Notification.Position.TOP_CENTER
        )
    }
    
    private fun updateItemsGrid() {
        itemsGrid.setItems(saleItems)
    }
    
    private fun updateTotal() {
        val total = saleItems.sumOf { it.getTotalPrice() }
        totalLabel.text = "TOTAL: $$total"
    }
    
    private fun clearSale() {
        saleItems.clear()
        customerName = null
        customerPhone = null
        customerInfoLabel?.let { updateCustomerInfoLabel(it) }
        updateItemsGrid()
        updateTotal()
        
        Notification.show("Venta limpiada", 2000, Notification.Position.TOP_CENTER)
    }
    
    private fun saveSale() {
        try {
            validateSale()
            showPaymentMethodDialog()
        } catch (e: Exception) {
            Notification.show(
                "Error al validar la venta: ${e.message}",
                5000,
                Notification.Position.TOP_CENTER
            )
        }
    }
    
    private fun showPaymentMethodDialog() {
        val dialog = Dialog()
        dialog.headerTitle = "💳 Método de Pago"
        dialog.element.style.set("--lumo-header-text-color", "var(--lumo-primary-text-color)")
        
        val content = VerticalLayout()
        content.setWidth("500px")
        content.isSpacing = true
        content.isPadding = false
        
        val totalAmount = saleItems.sumOf { it.getTotalPrice() }
        val totalLabel = Span("Total a pagar: $$totalAmount")
        totalLabel.element.style.set("font-size", "1.3em")
        totalLabel.element.style.set("font-weight", "bold")
        totalLabel.element.style.set("color", "var(--lumo-primary-text-color)")
        totalLabel.element.style.set("text-align", "center")
        totalLabel.element.style.set("margin-bottom", "20px")
        
        content.add(totalLabel)
        
        val instructionLabel = Span("Selecciona el método de pago:")
        instructionLabel.element.style.set("margin-bottom", "10px")
        instructionLabel.element.style.set("color", "var(--lumo-secondary-text-color)")
        content.add(instructionLabel)
        
        // Crear botones grandes para cada método de pago
        paymentMethods.forEach { method ->
            val button = Button(getPaymentMethodText(method)) {
                processSaleWithPaymentMethod(method)
                dialog.close()
            }
            
            // Estilo para botones grandes
            button.setWidthFull()
            button.element.style.set("height", "60px")
            button.element.style.set("font-size", "1.2em")
            button.element.style.set("margin", "5px 0")
            
            // Colores específicos por método de pago
            when (method) {
                "Efectivo" -> {
                    button.addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                    button.element.style.set("background", "var(--lumo-success-color)")
                    button.element.style.set("color", "white")
                }
                "Tarjeta" -> {
                    button.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                }
                "Transferencia" -> {
                    button.addThemeVariants(ButtonVariant.LUMO_CONTRAST)
                }
                else -> {
                    button.addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                }
            }
            
            content.add(button)
        }
        
        val cancelButton = Button("Cancelar") {
            dialog.close()
        }
        cancelButton.setWidthFull()
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY)
        cancelButton.element.style.set("margin-top", "15px")
        
        content.add(cancelButton)
        
        dialog.add(content)
        dialog.setCloseOnEsc(true)
        dialog.setCloseOnOutsideClick(false)
        dialog.open()
    }
    
    private fun getPaymentMethodText(method: String): String {
        return when (method) {
            "Efectivo" -> "💵 $method"
            "Tarjeta" -> "💳 $method"
            "Transferencia" -> "🏦 $method"
            "PayPal" -> "🟦 $method"
            "Mercado Pago" -> "🟨 $method"
            else -> method
        }
    }
    
    private fun processSaleWithPaymentMethod(paymentMethod: String) {
        try {
            val event = eventSelector.value
            val sale = Sale(
                event = event,
                saleDate = saleDateField.value,
                customerName = customerName,
                customerPhone = customerPhone,
                paymentMethod = paymentMethod,
                totalAmount = saleItems.sumOf { it.getTotalPrice() }
            )
            
            val items = saleItems.map { itemData ->
                SaleItem(
                    sale = sale,
                    product = itemData.product,
                    variant = itemData.variant,
                    quantity = itemData.quantity,
                    unitPrice = itemData.unitPrice,
                    totalPrice = itemData.getTotalPrice(),
                    personalization = itemData.personalization
                )
            }
            
            saleService.saveWithItems(sale, items)
            
            Notification.show(
                "¡Venta guardada exitosamente!\nMétodo: $paymentMethod\nTotal: $${sale.totalAmount}",
                5000,
                Notification.Position.TOP_CENTER
            )
            
            clearSale()
            
        } catch (e: Exception) {
            Notification.show(
                "Error al guardar: ${e.message}",
                5000,
                Notification.Position.TOP_CENTER
            )
        }
    }
    
    private fun validateSale() {
        require(eventSelector.value != null) { "Debe seleccionar un evento" }
        require(saleItems.isNotEmpty()) { "Debe agregar al menos un producto a la venta" }
        require(saleDateField.value != null) { "Debe especificar la fecha de venta" }
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
            
            Notification.show(
                "✅ Evento en curso seleccionado: ${currentEvent.name}",
                3000,
                Notification.Position.TOP_CENTER
            )
        }
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
                    Notification.show(
                        "Creando venta para: ${foundEvent.name}",
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