import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.nico.testpush"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nico.testpush"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug { isDebuggable = true }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // === Java 21 ===
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true   // necesario con minSdk < 26 cuando usás Java 21
    }

    // Flavors (si dejás los sufijos, asegurate que los google-services.json
    // tengan package_name com.nico.testpush.fiid0001 / .fiid0002)
    flavorDimensions += "entity"
    productFlavors {
        create("fiid0001") {
            dimension = "entity"
            applicationIdSuffix = ".fiid0001"
            resValue("string", "app_name", "TestPush 0001")
        }
        create("fiid0002") {
            dimension = "entity"
            applicationIdSuffix = ".fiid0002"
            resValue("string", "app_name", "TestPush 0002")
        }
    }
}

// === Kotlin en 21 usando Toolchain ===
kotlin {
    jvmToolchain(21)
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-messaging")

    // AndroidX UI
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // Desugaring para Java 21 en minSdk 24
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
