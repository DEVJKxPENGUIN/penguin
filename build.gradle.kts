import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("com.epages.restdocs-api-spec") version "0.19.2"
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
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring") //all-open
    apply(plugin = "kotlin-jpa")
    apply(plugin = "com.epages.restdocs-api-spec")

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-data-redis")
        implementation("org.springframework.session:spring-session-data-redis")
        implementation("org.springframework.boot:spring-boot-starter-cache")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("io.lettuce:lettuce-core")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("io.jsonwebtoken:jjwt:0.12.3")
        developmentOnly("org.springframework.boot:spring-boot-devtools")
        implementation("com.google.code.gson:gson")
        implementation("org.apache.commons:commons-lang3")
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.12")
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("com.h2database:h2")
        testImplementation("com.github.codemonstur:embedded-redis:1.4.2")
        testImplementation("org.assertj:assertj-core:3.27.3")
        testImplementation("org.mockito:mockito-inline:4.11.0")
        testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
        testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.2")
        implementation("com.mysql:mysql-connector-j:9.0.0")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        val summaryOnly = project.findProperty("summary") as? String == "true"

        if (!summaryOnly) {
            testLogging {
                // test jvm의 standard out and standard error을 console에 출력한다.
                events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
                showCauses = true
                showExceptions = true
                showStackTraces = true
                exceptionFormat = TestExceptionFormat.FULL
            }
        }

        reports.html.required.set(true)

        var totalTests = 0
        var totalPassed = 0
        var totalFailed = 0
        var totalSkipped = 0

        afterTest(KotlinClosure2<TestDescriptor, TestResult, Any>({ _, result ->
            totalTests++
            when (result.resultType) {
                TestResult.ResultType.SUCCESS -> totalPassed++
                TestResult.ResultType.FAILURE -> totalFailed++
                TestResult.ResultType.SKIPPED -> totalSkipped++
                else -> {}
            }
        }))

        afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, _ ->
            if (desc.parent == null) { // root suite
                val successRate = if (totalTests > 0) (totalPassed * 100) / totalTests else 0
                println("\n📊 Test Summary:")
                println("✅ PASSED : $totalPassed")
                println("❌ FAILED : $totalFailed")
                println("⏭️ SKIPPED: $totalSkipped")
                println("🧪 TOTAL  : $totalTests")
                println("📈 SUCCESS RATE: $successRate%\n")

                val reportFile = reports.html.outputLocation.get().asFile.resolve("index.html")
                if (reportFile.exists()) {
                    println("📂 Test report: file://$reportFile")
                }
            }
        }))

    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
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
