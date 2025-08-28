package com.perroamor.inventory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InventorySystemApplication

fun main(args: Array<String>) {
	runApplication<InventorySystemApplication>(*args)
}
