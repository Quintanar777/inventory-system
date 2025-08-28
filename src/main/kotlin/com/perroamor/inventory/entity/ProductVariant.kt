package com.perroamor.inventory.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "product_variants")
data class ProductVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,
    
    @Column(nullable = false)
    val variantName: String,
    
    @Column
    val color: String? = null,
    
    @Column
    val design: String? = null,
    
    @Column
    val material: String? = null,
    
    @Column
    val size: String? = null,
    
    @Column(precision = 10, scale = 2)
    val priceAdjustment: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false)
    val stock: Int,
    
    @Column
    val sku: String? = null,
    
    @Column
    val description: String? = null,
    
    @Column(name = "shopify_variant_id")
    val shopifyVariantId: String? = null,
    
    @Column(name = "shopify_product_id")
    val shopifyProductId: String? = null,
    
    @Column(name = "is_active")
    val isActive: Boolean = true
) {
    fun getEffectivePrice(): BigDecimal {
        return product.price.add(priceAdjustment)
    }
    
    fun getFullName(): String {
        return "${product.name} - $variantName"
    }
}