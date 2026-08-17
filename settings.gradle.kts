pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "deltabackend"

include(":services:identity-service")
include(":services:internal-treasury")
include(":services:customers-service")
include(":services:delta-secure")
include(":services:products")
include(":libs:grpc-contracts")
include(":libs:shared-auth")
