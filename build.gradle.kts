import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm") version "1.3.61"
    // https://imperceptiblethoughts.com/shadow/introduction/
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

val myShadow by configurations.creating {
    extendsFrom(configurations["shadow"], configurations["runtimeClasspath"])
    exclude("org.apache.solr")
    exclude("org.apache.tika")
    exclude("org.restlet.jee")
}

group = "org.github.aanno.solr.importplugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.3")
    api("org.apache.solr", "solr-core", "8.4.0") {
        exclude("org.restlet.jee", "org.restlet")
        exclude("org.restlet.jee", "org.restlet.ext.servlet")
    }
    api("org.apache.tika", "tika-core", "1.23")

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks {

    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    named<ShadowJar>("shadowJar") {
        manifest.attributes.apply {
            put("Implementation-Title", "Gradle Jar File Example")
            put("Implementation-Version", archiveVersion)
            put("Main-Class", "com.mkyong.DateUtils")
        }

        baseName = project.name + "-all"
        // archiveBaseName
        configurations = listOf(myShadow)
    }

    build {
        dependsOn(shadowJar)
    }
}

