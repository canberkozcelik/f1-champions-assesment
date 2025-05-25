import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
    id("jacoco")
}

group = "com.f1champions"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val mainClassName = "F1ChampionsApplication"
val mainClassPath = "com/f1champions/$mainClassName"

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        fileTree("${project.buildDir}/classes/kotlin/main") {
            exclude(
                "**/config/**",
                "**/dto/**",
                "**/common/**",
                "**/entity/**",
                "**/exception/**",
                "**/${mainClassPath}Kt.class",
                "**/$mainClassPath.class"
            )
        }
    )
    sourceDirectories.setFrom(files("src/main/kotlin"))
    executionData.setFrom(files("${project.buildDir}/jacoco/test.exec"))
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    violationRules {
        rule {
            limit {
                minimum = "0.7".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(
        fileTree("${project.buildDir}/classes/kotlin/main") {
            exclude(
                "**/config/**",
                "**/dto/**",
                "**/common/**",
                "**/entity/**",
                "**/exception/**",
                "**/${mainClassPath}Kt.class",
                "**/$mainClassPath.class"
            )
        }
    )
    executionData.setFrom(files("${project.buildDir}/jacoco/test.exec"))
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

jacoco {
    toolVersion = "0.8.11"
}

// Configure ktlint
ktlint {
    filter {
        exclude("**/package-info.kt")
    }
}
