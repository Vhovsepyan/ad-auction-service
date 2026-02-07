plugins {
	java
	id("org.springframework.boot") version "4.0.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example.auction"
version = "0.0.1-SNAPSHOT"
description = "ad-auction-service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// DB (JPA)
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")

	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}


tasks.withType<Test> {
	useJUnitPlatform()
}
