plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mbusino.mqttexplorer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mbusino.mqttbrowser"
        minSdk = 26
        targetSdk = 36
        // Play Store: versionCode increases with every release (v1.21 → 21, v1.22 → 22, ...)
        versionCode = 21
        versionName = "1.21"
    }

    signingConfigs {
        // Release signing reads credentials from ~/.gradle/gradle.properties
        // (mqttbrowserKeystoreFile / mqttbrowserStorePassword /
        //  mqttbrowserKeyPassword / mqttbrowserAlias).
        // Machines WITHOUT those properties skip this config entirely:
        // the release build then simply stays unsigned instead of failing.
        if (project.hasProperty("mqttbrowserKeystoreFile")) {
            create("release") {
                storeFile = file(project.property("mqttbrowserKeystoreFile") as String)
                storePassword = project.property("mqttbrowserStorePassword") as String
                keyAlias = project.property("mqttbrowserAlias") as String
                keyPassword = project.property("mqttbrowserKeyPassword") as String
            }
        }
    }

    buildTypes {
        release {
            // R8 intentionally OFF for the Play submission: Play performs its own
            // optimization, and enabling R8 for Paho without on-device testing is a risk.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Sign with the release key when the mqttbrowser* gradle properties are
            // present; otherwise skip signing (unsigned-ish release, no hard failure).
            if (project.hasProperty("mqttbrowserKeystoreFile")) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
