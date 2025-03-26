package com.example.connector.adapter.kafka

import com.example.connector.adapter.elastic.Location
import com.example.connector.adapter.elastic.RecognitionEventEntity
import com.example.connector.application.port.RecognitionsConsumer
import com.example.connector.application.port.RecognitionsRepository
import com.recognition.RecognitionEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class RecognitionsKafkaConsumer(private val recognitionsRepository: RecognitionsRepository) : RecognitionsConsumer {


    @KafkaListener(
        topics = ["recognitions"],
        containerFactory = "kafkaBatchListenerContainerFactory",

        )
    fun listenBatch(records: List<ConsumerRecord<String, RecognitionEvent>>) {
        if (records.isEmpty()) return

        val payloads = records.mapNotNull { it.value() }
        val deduplicated = payloads.distinctBy { it.uniqueId }

        recognitionsRepository.saveAll(deduplicated.map {
            RecognitionEventEntity(
                it.uniqueId,
                it.timestamp,
                it.source,
                Location(it.lat, it.lon),
                it.type,
                it.confidence
            )
        })
    }

}