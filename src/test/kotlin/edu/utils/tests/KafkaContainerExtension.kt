package edu.utils.tests

import org.testcontainers.containers.KafkaContainer

class KafkaContainerExtension : AbstractContainerAwareExtension<KafkaContainer>() {
    override fun start(): KafkaContainer {
        log.info("Starting container for Kafka")
        val kafka = KafkaContainer("5.3.1")
        kafka.start()

        System.setProperty("spring.kafka.producer.bootstrap-servers", kafka.bootstrapServers)
        System.setProperty("spring.kafka.consumer.bootstrap-servers", kafka.bootstrapServers)
        return kafka
    }
}
