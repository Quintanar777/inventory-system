plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vaadin") version "24.8.6"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.perroamor"

version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["vaadinVersion"] = "24.8.6"

// Configuración Vaadin
vaadin {
    productionMode = project.hasProperty("production") || project.hasProperty("vaadin.productionMode")
    pnpmEnable = true
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.vaadin:vaadin-spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configuración de empaquetado
tasks.withType<Jar> {
    enabled = true
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = true
    archiveFileName.set("${project.name}.jar")
    manifest {
        attributes(
            "Main-Class" to "org.springframework.boot.loader.launch.JarLauncher",
            "Start-Class" to "${project.group}.inventory.InventorySystemApplicationKt"
        )
    }
}

// Dependencias para asegurar el build del frontend
tasks.named("build") {
    dependsOn("vaadinBuildFrontend")
}

tasks.named("bootJar") {
    dependsOn("vaadinBuildFrontend")
}

// Task personalizado para Railway
tasks.register("railwayBuild") {
    group = "build"
    description = "Build for Railway deployment"
    dependsOn("clean", "vaadinBuildFrontend", "bootJar")
}