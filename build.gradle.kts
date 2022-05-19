import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project

plugins {
    kotlin("multiplatform") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
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
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
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

                runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:commonMain")
                runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:concurrentMain")
                runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.1")
                runtimeOnly("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

                implementation("com.pi4j:pi4j-core:2.1.1")
                implementation("com.pi4j:pi4j-plugin-raspberrypi:2.1.1")
                implementation("com.pi4j:pi4j-plugin-pigpio:2.1.1")
                implementation("com.pi4j:pi4j-plugin-mock:2.1.1")

                implementation("org.usb4java:usb4java:1.3.0")
                implementation("org.usb4java:usb4java-javax:1.3.0")
                implementation("com.igormaznitsa:jbbp:1.4.1")

                implementation(kotlin("test"))
                implementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.290-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.290-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.3.0-pre.325-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-css:17.0.2-pre.290-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.8.2-pre.325-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui:5.5.3-pre.325-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-icons:5.5.1-pre.325-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-muix-data-grid:5.10.0-pre.337")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-popper:2.2.5-pre.325-kotlin-1.6.10")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
                implementation(npm("js-cookie","3.0.1"))
//                implementation(npm("react-admin", "4.0.4"))
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

tasks.getByName<Jar>("jvmJar") {
    val taskName = if (project.hasProperty("isProduction")
        || project.gradle.startParameter.taskNames.contains("installDist")
    ) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
    from(File(webpackTask.destinationDirectory, webpackTask.outputFileName)) // bring output file along into the JAR
}