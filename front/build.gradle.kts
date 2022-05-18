import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("js")
//    kotlin("plugin.serialization")
}

group = "com.n0n5ense"
version = "0.0.1"

repositories {
    mavenCentral()
//    maven {
//        url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers/")
//    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js:1.6.21")
//    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
//    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.290-kotlin-1.6.10")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.290-kotlin-1.6.10")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-css:17.0.2-pre.290-kotlin-1.6.10")

    implementation(npm("react-constraint-layout","2.0.0"))
    implementation(npm("react", "17.0.2"))
    implementation(npm("react-dom", "17.0.2"))
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:1.0-M1-1.4.0-rc")

}

kotlin {
    js {
        browser {
            webpackTask {
                cssSupport.enabled = true
            }

            runTask {
                cssSupport.enabled = true
                devServer = KotlinWebpackConfig.DevServer(
                    port = 3000,
                    proxy = mutableMapOf("/api" to mapOf("target" to "http://localhost:8080")),
                    static = mutableListOf("$buildDir/processedResources/js/main")
                )
            }

            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }

//    js(LEGACY) {
//        binaries.executable()
//        browser {
//            commonWebpackConfig {
//                cssSupport.enabled = true
//            }
//        }
//    }
}