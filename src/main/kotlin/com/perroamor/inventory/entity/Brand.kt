package com.perroamor.inventory.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "brands")
data class Brand(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, unique = true)
    val name: String,
    
    @Column
    val description: String? = null,
    
    @Column(name = "is_active")
    val isActive: Boolean = true,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String = name
}