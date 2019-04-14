package edu.zoo.restriction

import org.springframework.data.jpa.repository.JpaRepository

interface RestrictionRepository : JpaRepository<Restriction, Long> {
    fun findAllBySensorId(sensorId: String): List<Restriction>
}