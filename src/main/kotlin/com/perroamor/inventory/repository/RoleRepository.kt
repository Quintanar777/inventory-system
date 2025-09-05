package com.perroamor.inventory.repository

import com.perroamor.inventory.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    
    fun findByName(name: String): Role?
    
    fun findByIsActiveTrue(): List<Role>
    
    @Query("SELECT r FROM Role r ORDER BY r.name ASC")
    fun findAllOrderByName(): List<Role>
    
    @Query("SELECT r FROM Role r WHERE r.isActive = true ORDER BY r.name ASC")
    fun findActiveOrderByName(): List<Role>
    
    fun existsByName(name: String): Boolean
}