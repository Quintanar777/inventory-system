package com.perroamor.inventory.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,
    
    @Column(name = "wholesale_price", nullable = false, precision = 10, scale = 2)
    val wholesalePrice: BigDecimal,
    
    @Column(nullable = false)
    val category: String,
    
    @Column(nullable = false)
    val brand: String,
    
    @Column(nullable = false)
    val stock: Int,
    
    @Column
    val description: String? = null,
    
    @Column(name = "can_be_personalized")
    val canBePersonalized: Boolean = false,
    
    @Column(name = "has_variants")
    val hasVariants: Boolean = false
)