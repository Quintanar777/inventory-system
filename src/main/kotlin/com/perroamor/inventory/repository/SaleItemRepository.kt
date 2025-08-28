package com.perroamor.inventory.repository

import com.perroamor.inventory.entity.SaleItem
import com.perroamor.inventory.entity.Sale
import com.perroamor.inventory.entity.Product
import com.perroamor.inventory.entity.ProductVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SaleItemRepository : JpaRepository<SaleItem, Long> {
    
    fun findBySaleId(saleId: Long): List<SaleItem>
    
    fun findBySale(sale: Sale): List<SaleItem>
    
    fun findByProduct(product: Product): List<SaleItem>
    
    fun findByVariant(variant: ProductVariant): List<SaleItem>
    
    fun findByProductId(productId: Long): List<SaleItem>
    
    fun findByVariantId(variantId: Long): List<SaleItem>
    
    @Query("SELECT si FROM SaleItem si WHERE si.sale.event.id = :eventId")
    fun findByEventId(@Param("eventId") eventId: Long): List<SaleItem>
    
    @Query("SELECT si FROM SaleItem si WHERE si.sale.event.id = :eventId AND si.product.id = :productId")
    fun findByEventIdAndProductId(
        @Param("eventId") eventId: Long,
        @Param("productId") productId: Long
    ): List<SaleItem>
    
    @Query("SELECT si FROM SaleItem si WHERE si.sale.event.id = :eventId AND si.variant.id = :variantId")
    fun findByEventIdAndVariantId(
        @Param("eventId") eventId: Long,
        @Param("variantId") variantId: Long
    ): List<SaleItem>
    
    @Query("SELECT SUM(si.quantity) FROM SaleItem si WHERE si.product.id = :productId AND si.sale.isCancelled = false")
    fun getTotalQuantitySoldByProduct(@Param("productId") productId: Long): Long?
    
    @Query("SELECT SUM(si.quantity) FROM SaleItem si WHERE si.variant.id = :variantId AND si.sale.isCancelled = false")
    fun getTotalQuantitySoldByVariant(@Param("variantId") variantId: Long): Long?
    
    @Query("SELECT SUM(si.quantity) FROM SaleItem si WHERE si.sale.event.id = :eventId AND si.product.id = :productId AND si.sale.isCancelled = false")
    fun getTotalQuantitySoldByEventAndProduct(
        @Param("eventId") eventId: Long,
        @Param("productId") productId: Long
    ): Long?
    
    @Query("SELECT SUM(si.quantity) FROM SaleItem si WHERE si.sale.event.id = :eventId AND si.variant.id = :variantId AND si.sale.isCancelled = false")
    fun getTotalQuantitySoldByEventAndVariant(
        @Param("eventId") eventId: Long,
        @Param("variantId") variantId: Long
    ): Long?
    
    @Query("SELECT si FROM SaleItem si WHERE si.personalization IS NOT NULL AND si.personalization != ''")
    fun findItemsWithPersonalization(): List<SaleItem>
    
    @Query("SELECT p.name, SUM(si.quantity) FROM SaleItem si JOIN si.product p WHERE si.sale.event.id = :eventId AND si.sale.isCancelled = false GROUP BY p.id, p.name ORDER BY SUM(si.quantity) DESC")
    fun getTopSellingProductsByEvent(@Param("eventId") eventId: Long): List<Array<Any>>
    
    @Query("SELECT v.variantName, SUM(si.quantity) FROM SaleItem si JOIN si.variant v WHERE si.sale.event.id = :eventId AND si.sale.isCancelled = false AND si.variant IS NOT NULL GROUP BY v.id, v.variantName ORDER BY SUM(si.quantity) DESC")
    fun getTopSellingVariantsByEvent(@Param("eventId") eventId: Long): List<Array<Any>>
}