package com.perroamor.inventory.service

import com.perroamor.inventory.entity.Sale
import com.perroamor.inventory.entity.SaleItem
import com.perroamor.inventory.entity.Event
import com.perroamor.inventory.repository.SaleRepository
import com.perroamor.inventory.repository.SaleItemRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional
class SaleService(
    @Autowired private val saleRepository: SaleRepository,
    @Autowired private val saleItemRepository: SaleItemRepository
) {
    
    fun findAll(): List<Sale> {
        return saleRepository.findAll()
    }
    
    fun findById(id: Long): Sale? {
        return saleRepository.findById(id).orElse(null)
    }
    
    fun findByEvent(event: Event): List<Sale> {
        return saleRepository.findByEvent(event)
    }
    
    fun findByEventId(eventId: Long): List<Sale> {
        return saleRepository.findByEventId(eventId)
    }
    
    fun findActiveSales(): List<Sale> {
        return saleRepository.findByIsCancelledFalse()
    }
    
    fun findActiveSalesByEvent(eventId: Long): List<Sale> {
        return saleRepository.findByEventIdAndIsCancelledFalse(eventId)
    }
    
    fun findUnpaidSales(): List<Sale> {
        return saleRepository.findByIsPaidFalse()
    }
    
    fun findByPaymentMethod(paymentMethod: String): List<Sale> {
        return saleRepository.findByPaymentMethod(paymentMethod)
    }
    
    fun findSalesBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): List<Sale> {
        return saleRepository.findSalesBetweenDates(startDate, endDate)
    }
    
    fun findEventSalesBetweenDates(eventId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<Sale> {
        return saleRepository.findEventSalesBetweenDates(eventId, startDate, endDate)
    }
    
    fun getTotalSalesAmountByEvent(eventId: Long): BigDecimal {
        return saleRepository.getTotalSalesAmountByEvent(eventId) ?: BigDecimal.ZERO
    }
    
    fun countSalesByEvent(eventId: Long): Long {
        return saleRepository.countSalesByEvent(eventId)
    }
    
    fun getPaymentMethodSummaryByEvent(eventId: Long): List<Array<Any>> {
        return saleRepository.getPaymentMethodSummaryByEvent(eventId)
    }
    
    fun findByCustomerInfo(searchTerm: String): List<Sale> {
        return saleRepository.findByCustomerInfo(searchTerm)
    }
    
    fun save(sale: Sale): Sale {
        validateSale(sale)
        return saleRepository.save(sale)
    }
    
    fun saveWithItems(sale: Sale, items: List<SaleItem>): Sale {
        validateSale(sale)
        validateSaleItems(items)
        
        // Calcular el total basado en los items
        val calculatedTotal = items.sumOf { it.totalPrice }
        val adjustedSale = sale.copy(totalAmount = calculatedTotal)
        
        val savedSale = saleRepository.save(adjustedSale)
        
        // Guardar los items asociados a la venta
        items.forEach { item ->
            val adjustedItem = item.copy(sale = savedSale)
            saleItemRepository.save(adjustedItem)
        }
        
        return savedSale
    }
    
    fun cancelSale(id: Long): Sale? {
        val sale = findById(id)
        return sale?.let {
            save(it.copy(isCancelled = true))
        }
    }
    
    fun markAsPaid(id: Long): Sale? {
        val sale = findById(id)
        return sale?.let {
            save(it.copy(isPaid = true))
        }
    }
    
    fun markAsUnpaid(id: Long): Sale? {
        val sale = findById(id)
        return sale?.let {
            save(it.copy(isPaid = false))
        }
    }
    
    fun delete(id: Long) {
        saleRepository.deleteById(id)
    }
    
    // Métodos para SaleItems
    fun findItemsBySale(sale: Sale): List<SaleItem> {
        return saleItemRepository.findBySale(sale)
    }
    
    fun findItemsBySaleId(saleId: Long): List<SaleItem> {
        return saleItemRepository.findBySaleId(saleId)
    }
    
    fun findSaleItems(saleId: Long): List<SaleItem> {
        return findItemsBySaleId(saleId)
    }
    
    fun saveItem(item: SaleItem): SaleItem {
        validateSaleItem(item)
        return saleItemRepository.save(item)
    }
    
    fun deleteItem(itemId: Long) {
        saleItemRepository.deleteById(itemId)
    }
    
    // Métodos de estadísticas
    fun getEventStatistics(eventId: Long): Map<String, Any> {
        val totalSales = countSalesByEvent(eventId)
        val totalAmount = getTotalSalesAmountByEvent(eventId)
        val paymentSummary = getPaymentMethodSummaryByEvent(eventId)
        val topProducts = saleItemRepository.getTopSellingProductsByEvent(eventId)
        val topVariants = saleItemRepository.getTopSellingVariantsByEvent(eventId)
        
        return mapOf(
            "totalSales" to totalSales,
            "totalAmount" to totalAmount,
            "paymentMethods" to paymentSummary,
            "topProducts" to topProducts,
            "topVariants" to topVariants
        )
    }
    
    fun getEventStatisticsByBrand(eventId: Long): Map<String, Map<String, Any>> {
        val allSales = findByEventId(eventId)
        val allItems = allSales.flatMap { sale -> 
            findItemsBySaleId(sale.id!!).map { item -> sale to item } 
        }
        
        // Agrupar por marca
        val itemsByBrand = allItems.groupBy { (_, item) -> item.product.brand }
        
        val brandStats = mutableMapOf<String, Map<String, Any>>()
        
        itemsByBrand.forEach { (brand, brandItems) ->
            val brandSales = brandItems.map { it.first }.distinctBy { it.id }
            val brandSaleItems = brandItems.map { it.second }
            
            val totalSales = brandSales.size.toLong()
            val totalAmount = brandSaleItems.sumOf { it.totalPrice }
            val totalQuantity = brandSaleItems.sumOf { it.quantity }
            
            // Métodos de pago para esta marca
            val paymentMethods = brandSales.groupBy { it.paymentMethod }
                .map { (method, sales) ->
                    arrayOf(
                        method,
                        sales.size.toLong(),
                        sales.sumOf { it.totalAmount }
                    )
                }
            
            brandStats[brand] = mapOf(
                "totalSales" to totalSales,
                "totalAmount" to totalAmount,
                "totalQuantity" to totalQuantity,
                "paymentMethods" to paymentMethods
            )
        }
        
        return brandStats
    }
    
    private fun validateSale(sale: Sale) {
        require(sale.totalAmount >= BigDecimal.ZERO) { "El total de la venta no puede ser negativo" }
        require(sale.paymentMethod.isNotBlank()) { "El método de pago es obligatorio" }
        require(sale.isValidForEvent()) { "La fecha de venta debe estar dentro del período del evento" }
        require(sale.discountAmount >= BigDecimal.ZERO) { "El descuento no puede ser negativo" }
        require(sale.taxAmount >= BigDecimal.ZERO) { "Los impuestos no pueden ser negativos" }
    }
    
    private fun validateSaleItems(items: List<SaleItem>) {
        require(items.isNotEmpty()) { "La venta debe tener al menos un artículo" }
        items.forEach { validateSaleItem(it) }
    }
    
    private fun validateSaleItem(item: SaleItem) {
        require(item.quantity > 0) { "La cantidad debe ser mayor a cero" }
        require(item.unitPrice >= BigDecimal.ZERO) { "El precio unitario no puede ser negativo" }
        require(item.totalPrice >= BigDecimal.ZERO) { "El precio total no puede ser negativo" }
        require(item.personalizationCost >= BigDecimal.ZERO) { "El costo de personalización no puede ser negativo" }
        require(item.itemDiscount >= BigDecimal.ZERO) { "El descuento del artículo no puede ser negativo" }
    }
}