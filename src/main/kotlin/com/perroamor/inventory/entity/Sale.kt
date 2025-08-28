package com.perroamor.inventory.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "sales")
data class Sale(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,
    
    @Column(name = "sale_date", nullable = false)
    val saleDate: LocalDateTime,
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    val totalAmount: BigDecimal,
    
    @Column(name = "customer_name")
    val customerName: String? = null,
    
    @Column(name = "customer_phone")
    val customerPhone: String? = null,
    
    @Column(name = "customer_email")
    val customerEmail: String? = null,
    
    @Column(name = "payment_method", nullable = false)
    val paymentMethod: String, // "Efectivo", "Tarjeta", "Transferencia", etc.
    
    @Column
    val notes: String? = null,
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    
    @Column(name = "tax_amount", precision = 10, scale = 2)
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    
    @Column(name = "is_paid")
    val isPaid: Boolean = true,
    
    @Column(name = "is_cancelled")
    val isCancelled: Boolean = false
) {
    fun getSubtotal(): BigDecimal {
        return totalAmount.subtract(taxAmount).add(discountAmount)
    }
    
    fun getCustomerDisplayName(): String {
        return when {
            !customerName.isNullOrBlank() -> customerName!!
            !customerPhone.isNullOrBlank() -> customerPhone!!
            !customerEmail.isNullOrBlank() -> customerEmail!!
            else -> "Cliente Anónimo"
        }
    }
    
    fun isValidForEvent(): Boolean {
        val saleLocalDate = saleDate.toLocalDate()
        return !saleLocalDate.isBefore(event.startDate) && !saleLocalDate.isAfter(event.endDate)
    }
}