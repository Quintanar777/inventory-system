package com.perroamor.inventory.security

import com.perroamor.inventory.service.UserService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class VaadinAuthenticationProvider(
    private val userService: UserService
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val username = authentication.name
        val password = authentication.credentials.toString()

        val user = userService.authenticate(username, password)
            ?: throw BadCredentialsException("Credenciales inválidas")

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        
        return UsernamePasswordAuthenticationToken(user, password, authorities)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}