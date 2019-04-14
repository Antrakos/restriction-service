package edu.zoo.restriction.service

import edu.zoo.restriction.Restriction
import edu.zoo.restriction.RestrictionRepository
import org.springframework.stereotype.Service

@Service
class RestrictionServiceImpl(
        private val repository: RestrictionRepository
) : RestrictionService {

    override fun findOne(id: Long): Restriction? = repository.findById(id).orElse(null)

    override fun finaAllForSensor(sensorId: String): List<Restriction> = repository.findAllBySensorId(sensorId)

    override fun save(restriction: Restriction): Restriction = repository.save(restriction)

    override fun delete(id: Long) = repository.deleteById(id)
}