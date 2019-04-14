package edu.zoo.restriction.service

import edu.zoo.restriction.Restriction

interface RestrictionService {
    fun findOne(id: Long): Restriction?
    fun finaAllForSensor(sensorId: String): List<Restriction>
    fun save(restriction: Restriction): Restriction
    fun delete(id: Long)
}