package com.perroamor.inventory.security

import com.perroamor.inventory.entity.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.stereotype.Service
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinServletRequest
import jakarta.servlet.http.HttpServletRequest

@Service
class SecurityService {

    fun getAuthenticatedUser(): User? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication != null && authentication.isAuthenticated && authentication.principal is User) {
            authentication.principal as User
        } else {
            null
        }
    }

    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated && authentication.principal !is String
    }

    fun hasRole(role: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.authorities?.any { 
            it.authority == "ROLE_$role" 
        } ?: false
    }

    fun hasAnyRole(vararg roles: String): Boolean {
        return roles.any { hasRole(it) }
    }

    fun isAdmin(): Boolean = hasRole("ADMIN")
    
    fun isManager(): Boolean = hasRole("MANAGER")
    
    fun isEmployee(): Boolean = hasRole("EMPLOYEE")

    fun canAccessInventory(): Boolean = isAdmin() || isManager()
    
    fun canAccessCatalogs(): Boolean = isAdmin() || isManager()
    
    fun canAccessReports(): Boolean = isAdmin() || isManager()
    
    fun canAccessUsers(): Boolean = isAdmin()
    
    fun canAccessSales(): Boolean = true // Todos pueden acceder a ventas

    fun logout() {
        try {
            // Obtener la autenticación actual
            val authentication = SecurityContextHolder.getContext().authentication
            
            // Obtener el request actual de Vaadin
            val request = VaadinServletRequest.getCurrent()
            
            if (request != null && authentication != null) {
                // Usar SecurityContextLogoutHandler para un logout completo
                val logoutHandler = SecurityContextLogoutHandler()
                logoutHandler.logout(request.httpServletRequest, null, authentication)
            } else {
                // Fallback: solo limpiar el contexto
                SecurityContextHolder.clearContext()
            }
            
            // Navegar al login después del logout
            UI.getCurrent()?.navigate("login")
        } catch (e: Exception) {
            // En caso de error, asegurar que se limpia el contexto y navegar al login
            SecurityContextHolder.clearContext()
            UI.getCurrent()?.navigate("login")
        }
    }

    fun getCurrentUsername(): String? {
        return getAuthenticatedUser()?.username
    }

    fun getCurrentUserFullName(): String? {
        return getAuthenticatedUser()?.fullName
    }

    fun getCurrentUserRole(): String? {
        return getAuthenticatedUser()?.role?.name
    }
}