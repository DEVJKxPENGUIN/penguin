tasks.getByName("bootJar") {
    enabled = true
}

dependencies {
    implementation(project(":penguin-core"))
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
}