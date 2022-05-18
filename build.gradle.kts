
plugins {
    kotlin("jvm") version "1.6.21" apply false
    kotlin("js") version "1.6.21" apply false
    kotlin("plugin.serialization") version "1.6.21" apply false
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}

allprojects {
    group = "com.n0n5ense"
    version = "0.0.1"
    repositories {
        mavenCentral()
    }
}

