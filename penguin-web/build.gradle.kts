tasks.getByName("bootJar") {
    enabled = true
}

dependencies {
    implementation(project(":penguin-core"))
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
}

openapi3 {
    title = "Penguin API"
    version = "v1.0.0"
    format = "yaml"
}