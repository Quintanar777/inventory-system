package com.perroamor.inventory.config

import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig : VaadinWebSecurity() {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    override fun configure(http: HttpSecurity) {
        // Configuración específica si es necesario
        http.authorizeHttpRequests { auth ->
            auth
                .requestMatchers(
                    "/images/**",
                    "/icons/**",
                    "/favicon.ico",
                    "/h2-console/**"
                ).permitAll()
        }
        
        super.configure(http)
        setLoginView(http, "/login")
        
        // Configurar redirección post-login
        http.formLogin { form ->
            form
                .loginPage("/login")
                .defaultSuccessUrl("/", true) // Redirigir siempre a la página principal
                .permitAll()
        }
    }
}