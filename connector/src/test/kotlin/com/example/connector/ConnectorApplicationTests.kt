package com.example.connector

import com.example.connector.application.port.RecognitionsRepository
import com.recognition.RecognitionEvent
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName


@SpringBootTest
@TestConfiguration
class ConnectorApplicationTests {

    @Autowired
    lateinit var repository: RecognitionsRepository

    companion object {

        @Container
        @ServiceConnection
        @JvmStatic
        val elasticsearch: ElasticsearchContainer =
            ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.12.0"))
                .withEnv("xpack.security.enabled", "false")
                .withEnv("discovery.type", "single-node")
                .waitingFor(
                    org.testcontainers.containers.wait.strategy.Wait.forHttp("/")
                        .forStatusCode(200)
                )

        @Container
        @ServiceConnection
        @JvmStatic
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.5"))
            .withNetwork(Network.SHARED)
            .withNetworkAliases("kafka-broker")


        @Container
        @JvmStatic
        val schemaRegistry = GenericContainer<Nothing>(
            DockerImageName.parse("confluentinc/cp-schema-registry:7.6.5")
        ).apply {
            dependsOn(kafka)
            withNetwork(kafka.network)
            withNetworkAliases("schema-registry")
            withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka-broker:9092")
            withExposedPorts(8081)
        }.also {
            it.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun setProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
            registry.add("spring.kafka.properties.schema.registry.url") {
                "http://${schemaRegistry.host}:${schemaRegistry.getMappedPort(8081)}"
            }
        }
    }

    @Test
    fun test() {
        val props = KafkaTestUtils.producerProps(kafka.bootstrapServers).apply {
            put("key.serializer", StringSerializer::class.java.name)
            put("value.serializer", KafkaAvroSerializer::class.java.name)
            put("schema.registry.url", "http://${schemaRegistry.host}:${schemaRegistry.getMappedPort(8081)}")
        }

        val producer = KafkaProducer<String, RecognitionEvent>(props)
        val record = RecognitionEvent(
            1672531199000L,
            "device-A",
            "test-dynamic-123",
            52.2297,
            21.0122,
            "car",
            0.91f
        )
        producer.send(ProducerRecord("recognitions", record.uniqueId, record)).get()
        producer.close()
        Thread.sleep(3000)

        val doc = repository.findByIdOrNull("test-dynamic-123")
        assertNotNull(doc, "Document not found in Elasticsearch!")
    }


}