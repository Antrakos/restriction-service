package edu.utils.tests

import edu.utils.tests.AnnotationUtils.findAnnotationIn
import edu.utils.tests.AnnotationUtils.getFirstLevelAnnotations
import edu.zoo.restriction.LoggerDelegate
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.Extensions
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.springframework.kafka.test.rule.KafkaEmbedded
import org.springframework.test.util.ReflectionTestUtils
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

class KafkaExtension : AfterAllCallback, BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        if (kafkaEmbedded != null) {
            return
        }

        val engineDescriptor = ReflectionTestUtils.getField(context.root, "testDescriptor") as EngineDescriptor?
        val testCount = findTestClasses(engineDescriptor!!)
                .filter { isKafkaTest(it) }
                .filter { notDisabled(it) }
                .count()
        runningTests = AtomicInteger(testCount)

        log.info("Starting embedded Kafka")
        kafkaEmbedded = KafkaEmbedded(1, false, 1, "dev.sensorData.json", "dev.warning.json")
        kafkaEmbedded!!.afterPropertiesSet()

        System.setProperty("spring.kafka.producer.bootstrap-servers", kafkaEmbedded!!.brokersAsString)
        System.setProperty("spring.kafka.consumer.bootstrap-servers", kafkaEmbedded!!.brokersAsString)
    }

    override fun afterAll(context: ExtensionContext) {
        if (runningTests!!.decrementAndGet() == 0) {
            log.info("Stopping embedded Kafka")
            kafkaEmbedded!!.after()
        }
    }

    companion object {
        val log by LoggerDelegate()

        private lateinit var runningTests: AtomicInteger
        private var kafkaEmbedded: KafkaEmbedded? = null

        private fun notDisabled(testClass: KClass<*>): Boolean {
            return getFirstLevelAnnotations(testClass)
                    .none { Disabled::class.java.isInstance(it) }
        }

        private fun findTestClasses(descriptor: EngineDescriptor): Sequence<KClass<*>> {
            return descriptor.children.asSequence()
                    .filter { ClassTestDescriptor::class.java.isInstance(it) }
                    .map { ClassTestDescriptor::class.java!!.cast(it) }
                    .map { it.testClass.kotlin }
        }

        private fun isKafkaTest(testClass: KClass<*>): Boolean {
            return (getExtendWithAnnotations(testClass) + getExtensionsAnnotations(testClass))
                    .map { it.value.asSequence() }
                    .flatten()
                    .any { KafkaExtension::class == it }
        }

        private fun getExtendWithAnnotations(testClass: KClass<*>): Sequence<ExtendWith> {
            return findAnnotationIn(testClass, ExtendWith::class)
        }

        private fun getExtensionsAnnotations(testClass: KClass<*>): Sequence<ExtendWith> {
            return findAnnotationIn(testClass, Extensions::class)
                    .map { it.value.asSequence() }
                    .flatten()
        }
    }
}
