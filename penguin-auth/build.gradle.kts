plugins {

}

dependencies {
    // 필요한 의존성 추가
    implementation("org.springframework.grpc:spring-grpc-server-web-spring-boot-starter")
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
    implementation("io.grpc:grpc-netty-shaded")
    modules {
        module("io.grpc:grpc-netty") {
            replacedBy("io.grpc:grpc-netty-shaded", "Use Netty shaded instead of regular Netty")
        }
    }
}

ext {
    set("springGrpcVersion", "0.5.0")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${ext["springGrpcVersion"]}")
    }
}