val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposedVersion: String by project

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.n0n5ense"
version = "0.0.1"
application {
    mainClass.set("com.n0n5ense.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    maven { url = uri("https://dl.bintray.com/kotlin/exposed/") }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-jackson:1.6.8")
    implementation("io.ktor:ktor-client-jackson:$ktor_version")

    implementation("com.pi4j:pi4j-core:2.1.1")
    implementation("com.pi4j:pi4j-plugin-raspberrypi:2.1.1")
    implementation("com.pi4j:pi4j-plugin-pigpio:2.1.1")

    implementation("org.usb4java","usb4java","1.3.0")
    implementation("org.usb4java","usb4java-javax","1.3.0")
    implementation("com.igormaznitsa","jbbp","1.4.1")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.n0n5ense.ApplicationKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}