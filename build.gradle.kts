plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0"
}

group = "com.reflective"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Keep using flat src/ layout from the original project
sourceSets {
    main {
        java {
            srcDirs("src")
        }
    }
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "Main"
    }
    archiveBaseName.set("reflective-agent")
    archiveClassifier.set("")
    archiveVersion.set("")
}
