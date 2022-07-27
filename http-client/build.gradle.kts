plugins {
    id("java")
}

group = "GrepConsole-http-client"
version = ""

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("GrepConsole-http-client")
}