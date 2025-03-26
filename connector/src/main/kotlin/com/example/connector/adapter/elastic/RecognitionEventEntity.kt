package com.example.connector.adapter.elastic

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "recognitions")
data class RecognitionEventEntity(

    @Id
    @Field(type = FieldType.Keyword)
    val uniqueId: String,

    @Field(type = FieldType.Date)
    val timestamp: Long,

    @Field(type = FieldType.Keyword)
    val source: String,

    @Field(type = FieldType.Object)
    val location: Location,

    @Field(type = FieldType.Keyword)
    val type: String,

    @Field(type = FieldType.Float)
    val confidence: Float
)

data class Location(
    val lat: Double,
    val lon: Double
)