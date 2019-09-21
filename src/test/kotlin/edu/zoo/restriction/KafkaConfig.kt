package edu.zoo.restriction

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.env.Environment
import org.springframework.kafka.annotation.KafkaListenerConfigurer
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.KafkaNull
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.messaging.converter.GenericMessageConverter
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory
import org.springframework.messaging.handler.annotation.support.HeadersMethodArgumentResolver
import org.springframework.messaging.handler.annotation.support.MessageMethodArgumentResolver
import org.springframework.messaging.handler.annotation.support.PayloadArgumentResolver
import org.springframework.stereotype.Component
import java.io.IOException

@Configuration
class KafkaConfig(
        private val environment: Environment,
        private val conversionService: JsonConversionService
) : KafkaListenerConfigurer {

    @Bean(name = ["sensorDataSender"])
    fun sensorDataSender(
            producerFactory: ProducerFactory<*, *>,
            @Value("\${sensor.data.topic}") sensorDataTopic: String
    ): KafkaTemplate<String, SensorData> {
        return createSender(producerFactory as ProducerFactory<String, SensorData>, sensorDataTopic)
    }

    @Bean
    fun jsonSerializer(objectMapper: ObjectMapper): JsonSerializer<Any> {
        val objectJsonSerializer = JsonSerializer<Any>(objectMapper)
        objectJsonSerializer.isAddTypeInfo = false
        return objectJsonSerializer
    }

    @Bean
    fun producerFactory(jsonSerializer: JsonSerializer<Any>): ProducerFactory<Any, Any> {
        return DefaultKafkaProducerFactory<Any, Any>(mapOf(
                "bootstrap.servers" to environment.getProperty("spring.kafka.producer.bootstrap-servers")
        )).apply {
            setValueSerializer(jsonSerializer)
            setKeySerializer(jsonSerializer)
        }

    }

    private fun <K, V> createSender(producerFactory: ProducerFactory<K, V>, topic: String) =
            KafkaTemplate(producerFactory).apply { defaultTopic = topic }

    override fun configureKafkaListeners(registrar: KafkaListenerEndpointRegistrar) {
        val messageConverter = GenericMessageConverter(conversionService)
        registrar.messageHandlerMethodFactory = DefaultMessageHandlerMethodFactory().apply {
            setConversionService(conversionService)
            setArgumentResolvers(listOf(
                    HeadersMethodArgumentResolver(),
                    MessageMethodArgumentResolver(messageConverter),
                    KafkaPayloadArgumentResolver(messageConverter)
            ))
        }
    }
}

@Component
class JsonConversionService(
        private val objectMapper: ObjectMapper
) : ConversionService {

    override fun canConvert(sourceType: Class<*>?, targetType: Class<*>) =
            sourceType == String::class.java

    override fun canConvert(sourceType: TypeDescriptor?, targetType: TypeDescriptor) =
            canConvert(sourceType?.objectType, targetType.objectType)

    override fun <T : Any?> convert(source: Any?, targetType: Class<T>) = try {
        objectMapper.readValue(source.toString(), targetType)
    } catch (e: IOException) {
        log.error("Failed to convert to: " + targetType.name + ", payload: " + source, e)
        null
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor?, targetType: TypeDescriptor) =
            convert(source!!, targetType.objectType as Class<Any>)

    companion object {
        val log by LoggerDelegate()
    }
}


class KafkaPayloadArgumentResolver(messageConverter: MessageConverter) : PayloadArgumentResolver(messageConverter) {
    override fun isEmptyPayload(payload: Any?) = payload == null || payload is KafkaNull
}
