plugins {
    id("com.google.protobuf") version "0.10.0" apply false
}

allprojects {
    group = "br.com.deltaglobalbank"
    version = "0.0.1-SNAPSHOT"
    extra["grpcVersion"] = "1.76.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}