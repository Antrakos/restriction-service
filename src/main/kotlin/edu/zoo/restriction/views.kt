package edu.zoo.restriction

import java.time.Duration
import java.time.Instant

data class SensorData(
        val id: String,
        val timestamp: Instant,
        val value: Double
)

data class RestrictionView(
        val id: Long,
        val limit: Long,
        val duration: Duration,
        val upperBound: Double?,
        val lowerBound: Double?
) {
    companion object {
        fun from(restriction: Restriction): RestrictionView = RestrictionView(
                id = restriction.id!!,
                limit = restriction.limit,
                duration = restriction.duration,
                upperBound = restriction.upperBound,
                lowerBound = restriction.lowerBound
        )
    }
}

data class Warning(
        val restriction: RestrictionView,
        val value: SensorData
)
