plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.3.0"
}

group = "GrepConsole"
version = "12.8.211.6693.3"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2021.3")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("java"))
}


dependencies {
    implementation("com.github.albfernandez:juniversalchardet:2.4.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.jctools:jctools-core:3.3.0")
    implementation("commons-beanutils:commons-beanutils:1.9.4")
    implementation("javax.media:jmf:2.1.1e")
    implementation("uk.com.robust-it:cloning:1.9.12")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildSearchableOptions {
        enabled = false
    }
}
