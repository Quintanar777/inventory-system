package com.perroamor.inventory.service

import com.perroamor.inventory.entity.Product
import com.perroamor.inventory.entity.ProductVariant
import com.perroamor.inventory.repository.ProductVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductVariantService(private val productVariantRepository: ProductVariantRepository) {
    
    fun findAll(): List<ProductVariant> = productVariantRepository.findAll()
    
    fun findById(id: Long): ProductVariant? = productVariantRepository.findById(id).orElse(null)
    
    fun findByProduct(product: Product): List<ProductVariant> = productVariantRepository.findByProduct(product)
    
    fun findActiveByProduct(product: Product): List<ProductVariant> = 
        productVariantRepository.findByProductAndIsActiveTrue(product)
    
    fun findByProductId(productId: Long): List<ProductVariant> = 
        productVariantRepository.findByProductId(productId)
    
    fun findActiveByProductId(productId: Long): List<ProductVariant> = 
        productVariantRepository.findByProductIdAndIsActiveTrue(productId)
    
    fun findInStock(): List<ProductVariant> = productVariantRepository.findInStock()
    
    fun findLowStock(): List<ProductVariant> = productVariantRepository.findLowStock()
    
    fun save(variant: ProductVariant): ProductVariant = productVariantRepository.save(variant)
    
    fun delete(id: Long) = productVariantRepository.deleteById(id)
    
    fun updateStock(id: Long, newStock: Int): ProductVariant? {
        val variant = findById(id)
        return variant?.let {
            val updatedVariant = it.copy(stock = newStock)
            save(updatedVariant)
        }
    }
    
    fun reduceStock(id: Long, quantity: Int): ProductVariant? {
        val variant = findById(id)
        return variant?.let {
            if (it.stock >= quantity) {
                val updatedVariant = it.copy(stock = it.stock - quantity)
                save(updatedVariant)
            } else {
                throw IllegalArgumentException("Stock insuficiente para variante ${it.variantName}. Stock actual: ${it.stock}, solicitado: $quantity")
            }
        }
    }
    
    fun deactivateVariant(id: Long): ProductVariant? {
        val variant = findById(id)
        return variant?.let {
            val deactivatedVariant = it.copy(isActive = false)
            save(deactivatedVariant)
        }
    }
    
    fun activateVariant(id: Long): ProductVariant? {
        val variant = findById(id)
        return variant?.let {
            val activatedVariant = it.copy(isActive = true)
            save(activatedVariant)
        }
    }
    
    fun getDistinctColors(): List<String> = productVariantRepository.findDistinctColors()
    
    fun getDistinctDesigns(): List<String> = productVariantRepository.findDistinctDesigns()
    
    fun findByColor(color: String): List<ProductVariant> = 
        productVariantRepository.findByColorIgnoreCase(color)
    
    fun findByDesign(design: String): List<ProductVariant> = 
        productVariantRepository.findByDesignContainingIgnoreCase(design)
}