package edu.zoo.restriction.stream

import edu.zoo.restriction.LoggerDelegate
import edu.zoo.restriction.RestrictionView
import edu.zoo.restriction.SensorData
import edu.zoo.restriction.Warning
import edu.zoo.restriction.service.RestrictionService
import edu.zoo.restriction.stream.SensorDataBinding.Companion.SENSOR_DATA_IN
import edu.zoo.restriction.stream.SensorDataBinding.Companion.WARNING_OUT
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KStream
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component


interface SensorDataBinding {
    @Input(SENSOR_DATA_IN)
    fun sensorData(): KStream<Long, SensorData>

    @Output(WARNING_OUT)
    fun warning(): KStream<Long, Warning>

    companion object {
        const val SENSOR_DATA_IN = "sensorData"
        const val WARNING_OUT = "warningQueue"
    }
}

@Component
@EnableBinding(SensorDataBinding::class)
class SensorDataProcessor(
        private val restrictionService: RestrictionService
) {
    @StreamListener(SENSOR_DATA_IN)
    @SendTo(WARNING_OUT)
    fun process(source: KStream<Long, SensorData>): KStream<Long, Warning> = source
            .peek { _, data -> log.debug("Received sensor data: {}", data) }
            .flatMapValues { data -> restrictionService.finaAllForSensor(data.id).map { data to it } }
            .filterNot { _, (sensorData, restriction) -> restriction.isWithinLimits(sensorData.value) }
            .mapValues { (sensorData, restriction) ->
                Warning(
                        restriction = RestrictionView.from(restriction),
                        value = sensorData
                )
            }
            .peek { _, warning -> log.debug("Sending warning: {}", warning) }
            .map { _, warning ->
                KeyValue(warning.restriction.id, warning)
            }

    companion object {
        val log by LoggerDelegate()
    }
}