plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.scrollloom"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.scrollloom"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register("verifyZeroNetworkDependencies") {
    group = "verification"
    description = "Ensures zero network and tracking dependencies exist in runtimeClasspath."
    doLast {
        val forbiddenTokens = listOf("okhttp", "retrofit", "ktor", "volley", "firebase", "analytics", "admob")
        val runtimeConfigs = configurations.filter { it.name.endsWith("RuntimeClasspath") }
        val violations = mutableListOf<String>()
        runtimeConfigs.forEach { config ->
            try {
                config.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                    val id = artifact.moduleVersion.id.toString().lowercase()
                    forbiddenTokens.forEach { token ->
                        if (id.contains(token)) {
                            violations.add("${config.name} -> $id")
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore configurations that cannot be resolved directly
            }
        }
        if (violations.isNotEmpty()) {
            throw GradleException("FATAL: Forbidden network dependencies detected: $violations. ScrollLoom enforces strict zero-network purity!")
        }
        println("✅ verifyZeroNetworkDependencies passed: 0 forbidden network dependencies found across runtime configurations.")
    }
}

tasks.named("preBuild") {
    dependsOn("verifyZeroNetworkDependencies")
}
