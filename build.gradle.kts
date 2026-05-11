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

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
    test {
        java {
            srcDirs("test")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "com.reflective.Main"
    }
    archiveBaseName.set("reflective-agent")
    archiveClassifier.set("")
    archiveVersion.set("")
}
