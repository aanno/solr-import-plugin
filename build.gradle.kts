plugins {
    java
    kotlin("jvm") version "1.3.61"
}

group = "org.github.aanno.solr.importplugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

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
}
