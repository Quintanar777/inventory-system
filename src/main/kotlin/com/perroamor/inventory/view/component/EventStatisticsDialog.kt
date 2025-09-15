package com.perroamor.inventory.view.component

import com.perroamor.inventory.entity.Event
import com.perroamor.inventory.service.SaleService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.tabs.TabsVariant
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class EventStatisticsDialog(
    private val saleService: SaleService
) {
    
    fun show(event: Event) {
        val dialog = Dialog()
        dialog.width = "90vw"
        dialog.height = "80vh"
        dialog.isResizable = true
        
        val content = VerticalLayout()
        content.setSizeFull()
        
        // Título
        content.add(H2("📊 Estadísticas del Evento: ${event.name}"))
        
        // Obtener estadísticas generales y por marca
        val generalStats = saleService.getEventStatistics(event.id!!)
        val brandStats = saleService.getEventStatisticsByBrand(event.id!!)
        
        // Crear pestañas
        val tabs = Tabs()
        tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS)
        tabs.setWidthFull()
        
        // Pestaña de totales
        val totalTab = Tab("📊 Total General")
        tabs.add(totalTab)
        
        // Pestañas por marca
        val brandTabs = mutableMapOf<Tab, String>()
        brandStats.keys.sorted().forEach { brand ->
            val brandTab = Tab("🏷️ $brand")
            tabs.add(brandTab)
            brandTabs[brandTab] = brand
        }
        
        // Contenido de las pestañas
        val tabContent = VerticalLayout()
        tabContent.setSizeFull()
        
        // Mostrar contenido inicial (total)
        showGeneralStatistics(tabContent, generalStats)
        
        // Listener para cambio de pestañas
        tabs.addSelectedChangeListener { event ->
            tabContent.removeAll()
            
            when (val selectedTab = event.selectedTab) {
                totalTab -> showGeneralStatistics(tabContent, generalStats)
                in brandTabs.keys -> {
                    val brand = brandTabs[selectedTab]!!
                    val stats = brandStats[brand]!!
                    showBrandStatistics(tabContent, brand, stats)
                }
            }
        }
        
        content.add(tabs, tabContent)
        
        // Botón cerrar
        val closeButton = Button("Cerrar") { dialog.close() }
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        content.add(HorizontalLayout(closeButton).apply {
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        })
        
        dialog.add(content)
        dialog.open()
    }
    
    private fun showGeneralStatistics(container: VerticalLayout, statistics: Map<String, Any>) {
        container.removeAll()
        
        val totalSales = statistics["totalSales"] as Long
        val totalAmount = statistics["totalAmount"] as BigDecimal
        val paymentMethods = statistics["paymentMethods"] as List<Array<Any>>
        
        // Estadísticas generales
        val generalStats = VerticalLayout()
        generalStats.add(H3("📈 Resumen General"))
        generalStats.add(
            createStatLine("🛒 Total de Ventas:", "$totalSales ventas"),
            createStatLine("💰 Ingresos Totales:", "$$totalAmount")
        )
        
        container.add(generalStats)
        
        // Estadísticas por método de pago
        if (paymentMethods.isNotEmpty()) {
            container.add(H3("💳 Ventas por Método de Pago"))
            
            val paymentStats = VerticalLayout()
            paymentMethods.forEach { methodData ->
                val paymentMethod = methodData[0] as String
                val count = methodData[1] as Long
                val amount = methodData[2] as BigDecimal
                
                val icon = getPaymentMethodIcon(paymentMethod)
                
                paymentStats.add(
                    createPaymentStatLine(
                        "$icon $paymentMethod:", 
                        "$count ventas", 
                        "$$amount"
                    )
                )
            }
            container.add(paymentStats)
        } else {
            container.add(Span("Sin ventas registradas para este evento"))
        }
    }
    
    private fun showBrandStatistics(container: VerticalLayout, brand: String, statistics: Map<String, Any>) {
        container.removeAll()
        
        val totalSales = statistics["totalSales"] as Long
        val totalAmount = statistics["totalAmount"] as BigDecimal
        val totalQuantity = statistics["totalQuantity"] as Int
        val paymentMethods = statistics["paymentMethods"] as List<Array<Any>>
        
        // Estadísticas de la marca
        val brandStats = VerticalLayout()
        brandStats.add(H3("🏷️ Estadísticas de $brand"))
        brandStats.add(
            createStatLine("🛒 Ventas de la marca:", "$totalSales ventas"),
            createStatLine("📦 Productos vendidos:", "$totalQuantity unidades"),
            createStatLine("💰 Ingresos de la marca:", "$$totalAmount")
        )
        
        container.add(brandStats)
        
        // Métodos de pago para esta marca
        if (paymentMethods.isNotEmpty()) {
            container.add(H3("💳 Métodos de Pago - $brand"))
            
            val paymentStats = VerticalLayout()
            paymentMethods.forEach { methodData ->
                val paymentMethod = methodData[0] as String
                val count = methodData[1] as Long
                val amount = methodData[2] as BigDecimal
                
                val icon = getPaymentMethodIcon(paymentMethod)
                
                paymentStats.add(
                    createPaymentStatLine(
                        "$icon $paymentMethod:", 
                        "$count ventas", 
                        "$$amount"
                    )
                )
            }
            container.add(paymentStats)
        } else {
            container.add(Span("Sin ventas registradas para la marca $brand"))
        }
    }
    
    private fun createStatLine(label: String, value: String): HorizontalLayout {
        val layout = HorizontalLayout()
        
        val labelSpan = Span(label)
        labelSpan.style.set("font-weight", "bold")
        
        val valueSpan = Span(value)
        valueSpan.style.set("color", "var(--lumo-primary-text-color)")
        valueSpan.style.set("font-weight", "bold")
        
        layout.add(labelSpan, valueSpan)
        layout.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
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
        layout.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        layout.setWidthFull()
        
        return layout
    }
    
    private fun getPaymentMethodIcon(paymentMethod: String): String {
        return when (paymentMethod.lowercase()) {
            "efectivo" -> "💵"
            "tarjeta", "tarjeta de débito", "tarjeta de crédito" -> "💳"
            "transferencia" -> "🏦"
            "paypal", "mercado pago" -> "📱"
            else -> "💰"
        }
    }
}