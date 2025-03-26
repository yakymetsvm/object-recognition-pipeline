package com.example.connector.adapter.elastic

import co.elastic.clients.elasticsearch.ElasticsearchClient
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class ElasticIndexInitializer(
    private val client: ElasticsearchClient
) {
    @PostConstruct
    fun init() {
        val indexName = "recognitions"

        val exists = client.indices().exists { it.index(indexName) }.value()
        if (!exists) {
            client.indices().create { create ->
                create.index("recognitions")
                    .settings { s ->
                        s.numberOfShards("3")
                            .numberOfReplicas("1")
                    }
                    .mappings { m ->
                        m.properties("timestamp") {
                            it.date { d -> d }
                        }
                        m.properties("source") {
                            it.keyword { k -> k }
                        }
                        m.properties("uniqueId") {
                            it.keyword { k -> k }
                        }
                        m.properties("location") {
                            it.geoPoint { gp -> gp }
                        }
                        m.properties("type") {
                            it.keyword { k -> k }
                        }
                        m.properties("confidence") {
                            it.float_ { f -> f }
                        }
                    }
            }
        }
    }
}