plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "com.f1champions.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            enableUnitTestCoverage = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Project modules
    api(project(":domain"))

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Networking
    api(libs.retrofit)
    api(libs.retrofit.converter.moshi)
    api(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.cash.turbine)
    testImplementation(kotlin("test"))
}

kover {
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                classes(
                    "*.R", "*.R$*", "*.BuildConfig", "*.Manifest", "*.*Manifest",
                    "*.Hilt_*.class", "*.*_HiltModules*", "*.*_Hilt_*",
                    "*.*_Factory", "*.*_MembersInjector", "*.*_Provide*",
                    "dagger.hilt.internal.*",
                )
                packages(
                    "com.f1champions.data.remote.exception",
                    "com.f1champions.data.di",
                    "com.f1champions.api.dto",
                    "hilt**",
                )
            }
        }
        verify {
            rule {
                minBound(70)
            }
        }
    }
}