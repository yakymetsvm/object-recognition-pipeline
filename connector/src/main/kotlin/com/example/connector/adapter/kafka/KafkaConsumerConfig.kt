package com.example.connector.adapter.kafka

import com.recognition.RecognitionEvent
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@Configuration
class KafkaConsumerConfig {

    @Value("\${spring.kafka.properties.schema.registry.url}")
    private lateinit var schemaRegistryUrl: String

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun recognitionConsumerFactory(): ConsumerFactory<String, RecognitionEvent> {
        val props = mapOf(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
            "schema.registry.url" to schemaRegistryUrl,
            "bootstrap.servers" to bootstrapServers,
            "specific.avro.reader" to true,
            ConsumerConfig.GROUP_ID_CONFIG to "recognition-connector",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaBatchListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, RecognitionEvent> {
        return ConcurrentKafkaListenerContainerFactory<String, RecognitionEvent>().apply {
            consumerFactory = recognitionConsumerFactory()
            isBatchListener = true
        }
    }
}