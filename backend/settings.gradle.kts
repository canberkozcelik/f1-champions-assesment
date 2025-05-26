rootProject.name = "f1-champions"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.22")
            version("spring-boot", "3.2.3")
            version("spring-dependency-management", "1.1.4")
            version("ktlint", "11.5.0")
            version("jacoco", "0.8.11")
        }
    }
}
