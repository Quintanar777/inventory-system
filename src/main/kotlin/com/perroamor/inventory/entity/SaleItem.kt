package com.perroamor.inventory.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "sale_items")
data class SaleItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false)
    val sale: Sale,
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_id", nullable = true)
    val variant: ProductVariant? = null,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    val unitPrice: BigDecimal,
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    val totalPrice: BigDecimal,
    
    @Column
    val personalization: String? = null, // Para grabados de nombre/teléfono
    
    @Column(name = "personalization_cost", precision = 10, scale = 2)
    val personalizationCost: BigDecimal = BigDecimal.ZERO,
    
    @Column(name = "item_discount", precision = 10, scale = 2)
    val itemDiscount: BigDecimal = BigDecimal.ZERO,
    
    @Column
    val notes: String? = null
) {
    fun getEffectiveUnitPrice(): BigDecimal {
        return if (variant != null) {
            variant!!.getEffectivePrice()
        } else {
            product.price
        }
    }
    
    fun getItemDescription(): String {
        return if (variant != null) {
            "${product.name} - ${variant!!.variantName}"
        } else {
            product.name
        }
    }
    
    fun getFullDescription(): String {
        val base = getItemDescription()
        return if (!personalization.isNullOrBlank()) {
            "$base (Personalizado: $personalization)"
        } else {
            base
        }
    }
    
    fun calculateSubtotal(): BigDecimal {
        return unitPrice.multiply(BigDecimal(quantity))
            .add(personalizationCost)
            .subtract(itemDiscount)
    }
    
    fun hasPersonalization(): Boolean {
        return !personalization.isNullOrBlank() && personalizationCost > BigDecimal.ZERO
    }
    
    fun getDisplayPrice(): String {
        val basePrice = calculateSubtotal()
        return if (hasPersonalization()) {
            "$${unitPrice} + $${personalizationCost} (personalización) = $${basePrice}"
        } else {
            "$${basePrice}"
        }
    }
}