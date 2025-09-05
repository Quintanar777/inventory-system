package com.perroamor.inventory.service

import com.perroamor.inventory.entity.User
import com.perroamor.inventory.entity.Role
import com.perroamor.inventory.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder
) {
    
    fun findAll(): List<User> = userRepository.findAllOrderByName()
    
    fun findActive(): List<User> = userRepository.findActiveOrderByName()
    
    fun findById(id: Long): User? = userRepository.findById(id).orElse(null)
    
    fun findByUsername(username: String): User? = userRepository.findByUsername(username)
    
    fun findByUsernameActive(username: String): User? = userRepository.findByUsernameAndIsActiveTrue(username)
    
    fun findByEmail(email: String): User? = userRepository.findByEmail(email)
    
    fun findByRole(role: Role): List<User> = userRepository.findByRole(role)
    
    fun findByRoleActive(role: Role): List<User> = userRepository.findByRoleAndIsActiveTrue(role)
    
    fun search(query: String): List<User> = userRepository.searchActiveUsers(query)
    
    fun save(user: User): User {
        val userToSave = if (user.id == 0L) {
            user.copy(
                password = if (isPasswordEncoded(user.password)) user.password else passwordEncoder.encode(user.password),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        } else {
            user.copy(updatedAt = LocalDateTime.now())
        }
        return userRepository.save(userToSave)
    }
    
    fun createUser(
        username: String,
        password: String,
        email: String,
        fullName: String,
        role: Role
    ): User {
        val user = User(
            username = username,
            password = passwordEncoder.encode(password),
            email = email,
            fullName = fullName,
            role = role
        )
        return save(user)
    }
    
    fun updatePassword(userId: Long, newPassword: String): User? {
        val user = findById(userId)
        return user?.let {
            val updatedUser = it.copy(
                password = passwordEncoder.encode(newPassword),
                updatedAt = LocalDateTime.now()
            )
            save(updatedUser)
        }
    }
    
    fun updateLastLogin(userId: Long): User? {
        val user = findById(userId)
        return user?.let {
            val updatedUser = it.copy(
                lastLogin = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            save(updatedUser)
        }
    }
    
    fun deactivate(id: Long): User? {
        val user = findById(id)
        return user?.let {
            val deactivatedUser = it.copy(isActive = false, updatedAt = LocalDateTime.now())
            save(deactivatedUser)
        }
    }
    
    fun activate(id: Long): User? {
        val user = findById(id)
        return user?.let {
            val activatedUser = it.copy(isActive = true, updatedAt = LocalDateTime.now())
            save(activatedUser)
        }
    }
    
    fun delete(id: Long) {
        userRepository.deleteById(id)
    }
    
    fun existsByUsername(username: String): Boolean = userRepository.existsByUsername(username)
    
    fun existsByEmail(email: String): Boolean = userRepository.existsByEmail(email)
    
    fun countActiveUsersByRole(roleName: String): Long = userRepository.countActiveUsersByRole(roleName)
    
    fun validatePassword(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
    
    fun authenticate(username: String, password: String): User? {
        val user = findByUsernameActive(username)
        return if (user != null && validatePassword(password, user.password)) {
            updateLastLogin(user.id)
            user
        } else {
            null
        }
    }
    
    private fun isPasswordEncoded(password: String): Boolean {
        // BCrypt passwords start with $2a$, $2b$, or $2y$
        return password.startsWith("\$2a\$") || password.startsWith("\$2b\$") || password.startsWith("\$2y\$")
    }
    
    fun initializeDefaultAdmin() {
        if (countActiveUsersByRole(Role.ADMIN) == 0L) {
            val adminRole = roleService.getAdminRole()
            if (adminRole != null && !existsByUsername("admin")) {
                createUser(
                    username = "admin",
                    password = "admin123",
                    email = "admin@perroamor.com",
                    fullName = "Administrador del Sistema",
                    role = adminRole
                )
            }
        }
    }
    
    // Métodos adicionales para UserManagementView
    fun createUser(
        username: String,
        password: String,
        email: String,
        fullName: String,
        role: Role,
        isActive: Boolean
    ): User {
        val user = User(
            username = username,
            password = passwordEncoder.encode(password),
            email = email,
            fullName = fullName,
            role = role,
            isActive = isActive
        )
        return save(user)
    }
    
    fun updateUser(
        id: Long,
        username: String,
        email: String,
        fullName: String,
        role: Role,
        isActive: Boolean
    ): User? {
        val user = findById(id)
        return user?.let {
            val updatedUser = it.copy(
                username = username,
                email = email,
                fullName = fullName,
                role = role,
                isActive = isActive,
                updatedAt = LocalDateTime.now()
            )
            save(updatedUser)
        }
    }
    
    fun updateUserStatus(id: Long, isActive: Boolean): User? {
        val user = findById(id)
        return user?.let {
            val updatedUser = it.copy(
                isActive = isActive,
                updatedAt = LocalDateTime.now()
            )
            save(updatedUser)
        }
    }
    
    fun resetPassword(id: Long, newPassword: String): User? {
        val user = findById(id)
        return user?.let {
            val updatedUser = it.copy(
                password = passwordEncoder.encode(newPassword),
                updatedAt = LocalDateTime.now()
            )
            save(updatedUser)
        }
    }

    // Métodos de conveniencia para obtener usuarios por rol
    fun getAdmins(): List<User> = roleService.getAdminRole()?.let { findByRoleActive(it) } ?: emptyList()
    fun getManagers(): List<User> = roleService.getManagerRole()?.let { findByRoleActive(it) } ?: emptyList()
    fun getEmployees(): List<User> = roleService.getEmployeeRole()?.let { findByRoleActive(it) } ?: emptyList()
}