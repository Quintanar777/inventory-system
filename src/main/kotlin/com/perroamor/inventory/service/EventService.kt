package com.perroamor.inventory.service

import com.perroamor.inventory.entity.Event
import com.perroamor.inventory.repository.EventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EventService(@Autowired private val eventRepository: EventRepository) {
    
    fun findAll(): List<Event> {
        return eventRepository.findAll()
    }
    
    fun findById(id: Long): Event? {
        return eventRepository.findById(id).orElse(null)
    }
    
    fun findActiveEvents(): List<Event> {
        return eventRepository.findByIsActiveTrue()
    }
    
    fun findCurrentEvents(): List<Event> {
        return eventRepository.findActiveEventsOnDate(LocalDate.now())
    }
    
    fun findUpcomingEvents(): List<Event> {
        return eventRepository.findUpcomingEvents()
    }
    
    fun findPastEvents(): List<Event> {
        return eventRepository.findPastEvents()
    }
    
    fun findByLocation(location: String): List<Event> {
        return eventRepository.findByLocationContainingIgnoreCase(location)
    }
    
    fun save(event: Event): Event {
        validateEvent(event)
        return eventRepository.save(event)
    }
    
    fun delete(id: Long) {
        eventRepository.deleteById(id)
    }
    
    fun deactivate(id: Long): Event? {
        val event = findById(id)
        return event?.let {
            save(it.copy(isActive = false))
        }
    }
    
    fun activate(id: Long): Event? {
        val event = findById(id)
        return event?.let {
            save(it.copy(isActive = true))
        }
    }
    
    private fun validateEvent(event: Event) {
        require(event.name.isNotBlank()) { "El nombre del evento no puede estar vacío" }
        require(event.location.isNotBlank()) { "La ubicación del evento no puede estar vacía" }
        require(!event.startDate.isAfter(event.endDate)) { 
            "La fecha de inicio no puede ser posterior a la fecha de fin" 
        }
    }
}