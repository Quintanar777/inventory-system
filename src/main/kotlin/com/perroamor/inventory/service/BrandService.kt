package com.perroamor.inventory.service

import com.perroamor.inventory.entity.Brand
import com.perroamor.inventory.repository.BrandRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class BrandService(private val brandRepository: BrandRepository) {
    
    fun findAll(): List<Brand> = brandRepository.findAllOrderByName()
    
    fun findActive(): List<Brand> = brandRepository.findActiveOrderByName()
    
    fun findById(id: Long): Brand? = brandRepository.findById(id).orElse(null)
    
    fun findByName(name: String): Brand? = brandRepository.findByName(name)
    
    fun search(query: String): List<Brand> = brandRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query)
    
    fun save(brand: Brand): Brand {
        val brandToSave = if (brand.id == 0L) {
            brand.copy(createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())
        } else {
            brand.copy(updatedAt = LocalDateTime.now())
        }
        return brandRepository.save(brandToSave)
    }
    
    fun deactivate(id: Long): Brand? {
        val brand = findById(id)
        return brand?.let {
            val deactivatedBrand = it.copy(isActive = false, updatedAt = LocalDateTime.now())
            save(deactivatedBrand)
        }
    }
    
    fun activate(id: Long): Brand? {
        val brand = findById(id)
        return brand?.let {
            val activatedBrand = it.copy(isActive = true, updatedAt = LocalDateTime.now())
            save(activatedBrand)
        }
    }
    
    fun delete(id: Long) {
        brandRepository.deleteById(id)
    }
    
    fun existsByName(name: String): Boolean {
        return brandRepository.findByName(name) != null
    }
    
    fun initializeDefaultBrands() {
        if (brandRepository.count() == 0L) {
            val defaultBrands = listOf(
                Brand(name = "Perro Amor", description = "Marca principal de productos para mascotas"),
                Brand(name = "Perra Madre", description = "Línea premium de accesorios")
            )
            
            defaultBrands.forEach { brand ->
                if (!existsByName(brand.name)) {
                    save(brand)
                }
            }
        }
    }
}