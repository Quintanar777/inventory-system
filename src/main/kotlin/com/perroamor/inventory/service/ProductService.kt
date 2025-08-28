package com.perroamor.inventory.service

import com.perroamor.inventory.entity.Product
import com.perroamor.inventory.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(private val productRepository: ProductRepository) {
    
    fun findAll(): List<Product> = productRepository.findAll()
    
    fun findById(id: Long): Product? = productRepository.findById(id).orElse(null)
    
    fun findByCategory(category: String): List<Product> = productRepository.findByCategory(category)
    
    fun findByBrand(brand: String): List<Product> = productRepository.findByBrand(brand)
    
    fun findByName(name: String): List<Product> = productRepository.findByNameContainingIgnoreCase(name)
    
    fun findInStock(): List<Product> = productRepository.findInStock()
    
    fun findLowStock(): List<Product> = productRepository.findLowStock()
    
    fun save(product: Product): Product = productRepository.save(product)
    
    fun delete(id: Long) = productRepository.deleteById(id)
    
    fun updateStock(id: Long, newStock: Int): Product? {
        val product = findById(id)
        return product?.let {
            val updatedProduct = it.copy(stock = newStock)
            save(updatedProduct)
        }
    }
    
    fun reduceStock(id: Long, quantity: Int): Product? {
        val product = findById(id)
        return product?.let {
            if (it.stock >= quantity) {
                val updatedProduct = it.copy(stock = it.stock - quantity)
                save(updatedProduct)
            } else {
                throw IllegalArgumentException("Stock insuficiente. Stock actual: ${it.stock}, solicitado: $quantity")
            }
        }
    }
}