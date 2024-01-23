version = "0.1.0"

plugins {
    id("java-library")
}

dependencies {
    api(project(":api"))

    compileOnly("de.hdskins.textureload:api:1.0.0") {
        exclude("net.labymod.labymod4")
    }
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}