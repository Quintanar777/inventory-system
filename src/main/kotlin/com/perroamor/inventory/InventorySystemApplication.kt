package com.perroamor.inventory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@SpringBootApplication
class InventorySystemApplication

@Component
class EnvironmentDebugger {
    @EventListener(ContextRefreshedEvent::class)
    fun debugEnvironment() {
        val dbVars = listOf(
            "DATABASE_URL", "DATABASE_PRIVATE_URL", "POSTGRES_URL",
            "PGUSER", "POSTGRES_USER", "DB_USER",
            "PGPASSWORD", "POSTGRES_PASSWORD", "DB_PASSWORD",
            "PORT"
        )
        
        println("=== DEBUG: Environment Variables ===")
        dbVars.forEach { varName ->
            val value = System.getenv(varName)
            if (value != null) {
                if (varName.contains("PASSWORD")) {
                    println("$varName=***HIDDEN***")
                } else {
                    println("$varName=$value")
                }
            } else {
                println("$varName=NOT_SET")
            }
        }
        println("=====================================")
    }
}

fun main(args: Array<String>) {
	runApplication<InventorySystemApplication>(*args)
}
