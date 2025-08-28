package com.perroamor.inventory.repository

import com.perroamor.inventory.entity.Sale
import com.perroamor.inventory.entity.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface SaleRepository : JpaRepository<Sale, Long> {
    
    fun findByEventId(eventId: Long): List<Sale>
    
    fun findByEvent(event: Event): List<Sale>
    
    fun findByIsCancelledFalse(): List<Sale>
    
    fun findByEventIdAndIsCancelledFalse(eventId: Long): List<Sale>
    
    fun findByIsPaidFalse(): List<Sale>
    
    fun findByPaymentMethod(paymentMethod: String): List<Sale>
    
    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.isCancelled = false")
    fun findSalesBetweenDates(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Sale>
    
    @Query("SELECT s FROM Sale s WHERE s.event.id = :eventId AND s.saleDate BETWEEN :startDate AND :endDate AND s.isCancelled = false")
    fun findEventSalesBetweenDates(
        @Param("eventId") eventId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Sale>
    
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.event.id = :eventId AND s.isCancelled = false AND s.isPaid = true")
    fun getTotalSalesAmountByEvent(@Param("eventId") eventId: Long): BigDecimal?
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.event.id = :eventId AND s.isCancelled = false")
    fun countSalesByEvent(@Param("eventId") eventId: Long): Long
    
    @Query("SELECT s.paymentMethod, COUNT(s), SUM(s.totalAmount) FROM Sale s WHERE s.event.id = :eventId AND s.isCancelled = false GROUP BY s.paymentMethod")
    fun getPaymentMethodSummaryByEvent(@Param("eventId") eventId: Long): List<Array<Any>>
    
    @Query("SELECT s FROM Sale s WHERE s.customerName LIKE %:searchTerm% OR s.customerPhone LIKE %:searchTerm% OR s.customerEmail LIKE %:searchTerm%")
    fun findByCustomerInfo(@Param("searchTerm") searchTerm: String): List<Sale>
}