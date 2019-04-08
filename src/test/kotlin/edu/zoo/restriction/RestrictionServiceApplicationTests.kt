package edu.zoo.restriction

import edu.zoo.restriction.service.RestrictionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@RestrictionServiceIntegrationTest
class RestrictionServiceApplicationTests {
    @Autowired
    private lateinit var sensorDataSender: KafkaTemplate<Long, SensorData>
    @Autowired
    private lateinit var restrictionService: RestrictionService
    private lateinit var countDownLatch: CountDownLatch
    private val warnings: MutableList<Warning> = ArrayList()


    @KafkaListener(topics = ["\${warning.topic}"], groupId = "test")
    fun receive(event: Warning) {
        log.info("Received warning: {}", event)
        countDownLatch.countDown()
        warnings.add(event)
    }

    @Test
    fun contextLoads() {
        val restrictions = listOf(
                createRestriction(10.0, 100.0),
                createRestriction(upperBound = 50.0),
                createRestriction(lowerBound = 50.0)
        ).map(restrictionService::save)

        val data = (0 until 100)
                .asSequence()
                .map { Random.nextDouble(0.0, 100.0) }
                .map { createSensorData(it) }
                .toList()
        val expected = data.asSequence()
                .flatMap { sensorData -> restrictions.asSequence().map { sensorData to it } }
                .filterNot { (sensorData, restriction) -> restriction.isWithinLimits(sensorData.value) }
                .map { (sensorData, restriction) -> Warning(restriction = RestrictionView.from(restriction), value = sensorData) }
                .toList()

        countDownLatch = CountDownLatch(expected.size)
        log.info("Expecting {} warnings from {} events", expected.size, data.size)
        data.parallelStream().forEach { sensorDataSender.sendDefault(it.id, it) }
        sensorDataSender.flush()

        assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue()
        assertThat(warnings).containsExactlyInAnyOrderElementsOf(expected)
    }

    private fun createSensorData(value: Double) = SensorData(id = SENSOR_ID, value = value, timestamp = ZonedDateTime.now().toInstant())
    private fun createRestriction(lowerBound: Double? = null, upperBound: Double? = null) = Restriction(
            id = null,
            duration = Duration.ZERO,
            limit = 0,
            sensorId = SENSOR_ID,
            lowerBound = lowerBound,
            upperBound = upperBound
    )

    companion object {
        const val SENSOR_ID = 1L
        val log by LoggerDelegate()
    }
}
