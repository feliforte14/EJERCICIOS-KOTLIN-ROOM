plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.ejercicioskotlinroom"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.ejercicioskotlinroom"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    // Para los tests de migración (ej. 14): el MigrationTestHelper lee los esquemas exportados como assets
    sourceSets {
        getByName("androidTest").assets.directories.add("$projectDir/schemas")
    }
}

// exportSchema = true escribe el JSON del esquema de cada versión en app/schemas
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Room: runtime + ktx (suspend/Flow) y el compilador con KSP (genera la implementación del DAO y la base)
    // room-testing (MigrationTestHelper) lee los JSON de esquema con kotlinx.serialization 1.8+, pero la resolución
    // consistente de Gradle lo bajaba a 1.7.3 por las dependencias de lifecycle. Subirlo acá en la app lo arregla
    // (sin esto el test de migración falla con AbstractMethodError).
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
