package com.perroamor.inventory.repository

import com.perroamor.inventory.entity.User
import com.perroamor.inventory.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    fun findByUsername(username: String): User?
    
    fun findByEmail(email: String): User?
    
    fun findByUsernameAndIsActiveTrue(username: String): User?
    
    fun findByIsActiveTrue(): List<User>
    
    fun findByRole(role: Role): List<User>
    
    fun findByRoleAndIsActiveTrue(role: Role): List<User>
    
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.fullName ASC")
    fun findActiveOrderByName(): List<User>
    
    @Query("SELECT u FROM User u ORDER BY u.fullName ASC")
    fun findAllOrderByName(): List<User>
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "u.isActive = true")
    fun searchActiveUsers(@Param("search") search: String): List<User>
    
    fun existsByUsername(username: String): Boolean
    
    fun existsByEmail(email: String): Boolean
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName AND u.isActive = true")
    fun countActiveUsersByRole(@Param("roleName") roleName: String): Long
}