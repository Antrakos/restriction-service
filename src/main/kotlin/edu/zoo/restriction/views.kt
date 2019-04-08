package edu.zoo.restriction

import java.time.Duration
import java.time.Instant

data class SensorData(
        val id: Long,
        val timestamp: Instant,
        val value: Double
)

data class RestrictionView(
        val id: Long,
        val limit: Long,
        val duration: Duration
) {
    companion object {
        fun from(restriction: Restriction): RestrictionView = RestrictionView(
                id = restriction.id!!,
                limit = restriction.limit,
                duration = restriction.duration
        )
    }
}

data class Warning(
        val restriction: RestrictionView,
        val value: SensorData
)
