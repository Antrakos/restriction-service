package edu.zoo.restriction

import java.time.Duration
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Restriction(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long?,
        val sensorId: String,
        @Column(name = "count_limit")
        val limit: Long,
        val duration: Duration,
        val upperBound: Double?,
        val lowerBound: Double?
) {
    fun isWithinLimits(value: Double) = lowerBound ?: Double.MIN_VALUE <= value && value <= upperBound ?: Double.MAX_VALUE
}
