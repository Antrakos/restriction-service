package edu.zoo.restriction.service

import edu.zoo.restriction.Restriction

interface RestrictionService {
    fun findOne(id: Long): Restriction?
    fun finaAllForSensor(sensorId: Long): List<Restriction>
    fun save(restriction: Restriction): Restriction
    fun delete(id: Long)
}