plugins {
    kotlin("multiplatform") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "com.transportpay"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()
    
    js(IR) {
        moduleName = "transport-pay-frontend"
        browser()
        binaries.executable()
    }

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                
                // Ktor Client Core
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                
                // Kotlinx Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                
                // Koin для DI
                implementation("io.insert-koin:koin-core:3.5.3")
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-js:2.3.7")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.7.3")
            }
        }

        val desktopMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-cio:2.3.7")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
                implementation("com.fazecast:jSerialComm:2.10.4")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.transportpay.MainKt"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
