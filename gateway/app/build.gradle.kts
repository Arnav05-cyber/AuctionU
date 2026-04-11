plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
}

group = "com.arnav"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

// CRITICAL: We need the Spring Cloud BOM for Gateway to work
extra["springCloudVersion"] = "2023.0.0"

dependencies {
    // 1. Reactive Gateway (Netty)
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // 2. Redis for Rate Limiting
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // 3. JWT tools
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // 4. Utils
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}