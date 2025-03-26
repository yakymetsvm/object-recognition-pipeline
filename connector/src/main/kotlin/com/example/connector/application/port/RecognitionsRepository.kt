package com.example.connector.application.port

import com.example.connector.adapter.elastic.RecognitionEventEntity
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Repository
interface RecognitionsRepository: ElasticsearchRepository<RecognitionEventEntity, String> {
}