val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project

plugins {
    kotlin("multiplatform") version "1.6.10"
    application
}

group = "com.n0n5ense"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
//    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
//    maven { url = uri("https://dl.bintray.com/kotlin/exposed/") }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
        withJava()
    }
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.5")

                implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")

                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

                implementation("org.xerial:sqlite-jdbc:3.36.0.3")
                implementation("org.mindrot:jbcrypt:0.4")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-jackson:1.6.8")
                implementation("io.ktor:ktor-client-jackson:$ktorVersion")

                implementation("com.pi4j:pi4j-core:2.1.1")
                implementation("com.pi4j:pi4j-plugin-raspberrypi:2.1.1")
                implementation("com.pi4j:pi4j-plugin-pigpio:2.1.1")
                implementation("com.pi4j:pi4j-plugin-mock:2.1.1")

                implementation("org.usb4java:usb4java:1.3.0")
                implementation("org.usb4java:usb4java-javax:1.3.0")
                implementation("com.igormaznitsa:jbbp:1.4.1")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.290-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.290-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-css:17.0.2-pre.290-kotlin-1.6.10")
            }
        }
    }
}

application {
    mainClass.set("com.n0n5ense.ApplicationKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}

dependencies {
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}