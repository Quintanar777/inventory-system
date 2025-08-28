package com.perroamor.inventory.repository

import com.perroamor.inventory.entity.Product
import com.perroamor.inventory.entity.ProductVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductVariantRepository : JpaRepository<ProductVariant, Long> {
    
    fun findByProduct(product: Product): List<ProductVariant>
    
    fun findByProductAndIsActiveTrue(product: Product): List<ProductVariant>
    
    fun findByProductIdAndIsActiveTrue(productId: Long): List<ProductVariant>
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId")
    fun findByProductId(@Param("productId") productId: Long): List<ProductVariant>
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.stock <= 5 AND pv.isActive = true")
    fun findLowStock(): List<ProductVariant>
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.stock > 0 AND pv.isActive = true")
    fun findInStock(): List<ProductVariant>
    
    fun findByColorIgnoreCase(color: String): List<ProductVariant>
    
    fun findByDesignContainingIgnoreCase(design: String): List<ProductVariant>
    
    @Query("SELECT DISTINCT pv.color FROM ProductVariant pv WHERE pv.color IS NOT NULL AND pv.isActive = true")
    fun findDistinctColors(): List<String>
    
    @Query("SELECT DISTINCT pv.design FROM ProductVariant pv WHERE pv.design IS NOT NULL AND pv.isActive = true")
    fun findDistinctDesigns(): List<String>
}