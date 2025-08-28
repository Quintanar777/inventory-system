package com.perroamor.inventory.repository

import com.perroamor.inventory.entity.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface EventRepository : JpaRepository<Event, Long> {
    
    fun findByIsActiveTrue(): List<Event>
    
    fun findByLocationContainingIgnoreCase(location: String): List<Event>
    
    @Query("SELECT e FROM Event e WHERE e.startDate <= :date AND e.endDate >= :date AND e.isActive = true")
    fun findActiveEventsOnDate(date: LocalDate): List<Event>
    
    @Query("SELECT e FROM Event e WHERE e.endDate >= :date AND e.isActive = true ORDER BY e.startDate ASC")
    fun findUpcomingEvents(date: LocalDate = LocalDate.now()): List<Event>
    
    @Query("SELECT e FROM Event e WHERE e.endDate < :date ORDER BY e.startDate DESC")
    fun findPastEvents(date: LocalDate = LocalDate.now()): List<Event>
}