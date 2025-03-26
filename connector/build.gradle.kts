plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.8.0"

}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal() // Needed for the Avro plugin
    maven("https://packages.confluent.io/maven")

}

dependencies {
    // Core
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams")
    implementation("com.github.davidmc24.gradle.plugin:gradle-avro-plugin:1.9.1")
    // Avro + Schema Registry
    implementation("org.apache.avro:avro:1.11.1")
    implementation("io.confluent:kafka-avro-serializer:7.2.1")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.testcontainers:junit-jupiter:1.19.0")
    testImplementation("org.testcontainers:kafka:1.19.0")
    testImplementation("org.testcontainers:elasticsearch:1.19.0")
    testImplementation("org.springframework.boot:spring-boot-testcontainers:3.4.4")
    implementation("org.springframework.boot:spring-boot-docker-compose:3.1.0")

}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-jammy-base:latest")
}


avro {
    stringType.set("String")
    fieldVisibility.set("PRIVATE")
}