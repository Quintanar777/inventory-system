package com.perroamor.inventory.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "events")
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,
    
    @Column(name = "end_date", nullable = false)
    val endDate: LocalDate,
    
    @Column(nullable = false)
    val location: String,
    
    @Column
    val description: String? = null,
    
    @Column(name = "is_active")
    val isActive: Boolean = true
) {
    fun isCurrentlyActive(): Boolean {
        val today = LocalDate.now()
        return isActive && !today.isBefore(startDate) && !today.isAfter(endDate)
    }
    
    fun getDurationInDays(): Long {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
    }
}