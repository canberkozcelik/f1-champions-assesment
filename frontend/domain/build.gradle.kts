plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "com.f1champions.domain"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
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
    // Kotlin
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.javax.inject)

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
                    "com.f1champions.domain.exception",
                    "com.f1champions.domain.model",
                    "com.f1champions.domain.repository",
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