package com.perroamor.inventory.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, unique = true)
    val username: String,
    
    @Column(nullable = false)
    val password: String,
    
    @Column(nullable = false)
    val email: String,
    
    @Column(name = "full_name", nullable = false)
    val fullName: String,
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    val role: Role,
    
    @Column(name = "is_active")
    val isActive: Boolean = true,
    
    @Column(name = "last_login")
    val lastLogin: LocalDateTime? = null,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String = "$fullName ($username)"
    
    fun hasRole(roleName: String): Boolean = role.name == roleName
    
    fun isAdmin(): Boolean = hasRole(Role.ADMIN)
    fun isManager(): Boolean = hasRole(Role.MANAGER)
    fun isEmployee(): Boolean = hasRole(Role.EMPLOYEE)
    
    fun canAccessInventory(): Boolean = isAdmin() || isManager()
    fun canAccessCatalogs(): Boolean = isAdmin() || isManager()
    fun canAccessReports(): Boolean = isAdmin() || isManager()
    fun canAccessUsers(): Boolean = isAdmin()
    fun canAccessSales(): Boolean = true // Todos pueden acceder a ventas
}