tasks.getByName("bootJar") {
    enabled = true
}

dependencies {
    implementation(project(":penguin-core"))
}

openapi3 {
    title = "Penguin API"
    version = "v1.0.0"
    format = "yaml"
    outputDirectory = "$projectDir/src/main/resources/static"
    setServer("http://localhost:8081")
}