import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

allprojects {
    group = "com.penguin"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

project(":penguin-web") {
    dependencies {
        implementation(project(":penguin-core"))
    }
}

project(":penguin-auth") {
    dependencies {
        implementation(project(":penguin-core"))
    }
}

project(":penguin-core") {
    val jar: Jar by tasks
    val bootJar: BootJar by tasks

    bootJar.enabled = false
    jar.enabled = true
}