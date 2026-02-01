import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    kotlin("kapt")
}

android {
    namespace = "com.callunlimited"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.callunlimited"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load secrets from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        
        // Load secrets from secrets.properties
        val secretsProperties = Properties()
        val secretsPropertiesFile = rootProject.file("secrets.properties")
        if (secretsPropertiesFile.exists()) {
            secretsProperties.load(FileInputStream(secretsPropertiesFile))
        }

        // Ensure BASE_URL is valid to prevent Retrofit crash at startup
        // It won't be used because we use dynamic @Url for credentials, but Retrofit Builder requires it.
        val baseUrl = project.findProperty("BASE_URL")?.toString() ?: "https://placeholder.com/"
        
        // Prefer local.properties, then secrets.properties, fallback to project property
        val credFile = localProperties.getProperty("CREDENTIAL_URL") 
            ?: secretsProperties.getProperty("CREDENTIAL_URL")
            ?: project.findProperty("CRED_FILE")?.toString() ?: ""
            
        val logoUrl = project.findProperty("LOGO_URL")?.toString() ?: ""

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "CRED_FILE", "\"$credFile\"")
        buildConfigField("String", "LOGO_URL", "\"$logoUrl\"")

        // Optimization: Keep only English resources to save space
        resConfigs("en")

        // Optimization: Only include necessary native architectures for production
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true 
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "**/androidx.compose.material_material-icons-extended.version"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    
    // Optimization: material-icons-extended is huge (~30MB). 
    // Code shrinking (R8) will remove unused icons, but including only specific icons is better.
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.coil.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation(libs.firebase.messaging)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Linphone
    implementation(libs.linphone.sdk)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    correctErrorTypes = true
}
