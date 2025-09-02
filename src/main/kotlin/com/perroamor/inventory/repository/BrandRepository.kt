package com.perroamor.inventory.repository

import com.perroamor.inventory.entity.Brand
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BrandRepository : JpaRepository<Brand, Long> {
    
    fun findByIsActiveTrue(): List<Brand>
    
    fun findByNameContainingIgnoreCaseAndIsActiveTrue(name: String): List<Brand>
    
    fun findByName(name: String): Brand?
    
    @Query("SELECT b FROM Brand b ORDER BY b.name ASC")
    fun findAllOrderByName(): List<Brand>
    
    @Query("SELECT b FROM Brand b WHERE b.isActive = true ORDER BY b.name ASC")
    fun findActiveOrderByName(): List<Brand>
}