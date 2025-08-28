package com.perroamor.inventory.repository

import com.perroamor.inventory.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    
    fun findByCategory(category: String): List<Product>
    
    fun findByBrand(brand: String): List<Product>
    
    fun findByNameContainingIgnoreCase(name: String): List<Product>
    
    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    fun findInStock(): List<Product>
    
    @Query("SELECT p FROM Product p WHERE p.stock <= 5")
    fun findLowStock(): List<Product>
}