package com.perroamor.inventory.service

import com.perroamor.inventory.entity.Role
import com.perroamor.inventory.repository.RoleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class RoleService(private val roleRepository: RoleRepository) {
    
    fun findAll(): List<Role> = roleRepository.findAllOrderByName()
    
    fun findActive(): List<Role> = roleRepository.findActiveOrderByName()
    
    fun findById(id: Long): Role? = roleRepository.findById(id).orElse(null)
    
    fun findByName(name: String): Role? = roleRepository.findByName(name)
    
    fun save(role: Role): Role {
        val roleToSave = if (role.id == 0L) {
            role.copy(createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())
        } else {
            role.copy(updatedAt = LocalDateTime.now())
        }
        return roleRepository.save(roleToSave)
    }
    
    fun deactivate(id: Long): Role? {
        val role = findById(id)
        return role?.let {
            val deactivatedRole = it.copy(isActive = false, updatedAt = LocalDateTime.now())
            save(deactivatedRole)
        }
    }
    
    fun activate(id: Long): Role? {
        val role = findById(id)
        return role?.let {
            val activatedRole = it.copy(isActive = true, updatedAt = LocalDateTime.now())
            save(activatedRole)
        }
    }
    
    fun delete(id: Long) {
        roleRepository.deleteById(id)
    }
    
    fun existsByName(name: String): Boolean = roleRepository.existsByName(name)
    
    fun initializeDefaultRoles() {
        if (roleRepository.count() == 0L) {
            val defaultRoles = listOf(
                Role(name = Role.ADMIN, description = "Administrador con acceso completo al sistema"),
                Role(name = Role.MANAGER, description = "Gerente con acceso a operaciones e inventario"),
                Role(name = Role.EMPLOYEE, description = "Empleado con acceso limitado a ventas")
            )
            
            defaultRoles.forEach { role ->
                if (!existsByName(role.name)) {
                    save(role)
                }
            }
        }
    }
    
    // Métodos de conveniencia para obtener roles específicos
    fun getAdminRole(): Role? = findByName(Role.ADMIN)
    fun getManagerRole(): Role? = findByName(Role.MANAGER)
    fun getEmployeeRole(): Role? = findByName(Role.EMPLOYEE)
}