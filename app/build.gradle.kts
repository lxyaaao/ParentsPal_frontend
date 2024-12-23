plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

dependencies {
//    implementation(libs.androidx.ui.test.junit4.android)
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.compiler)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.material3)
    implementation("androidx.compose.material3:material3-window-size-class:1.3.0")
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("com.google.guava:guava:27.0.1-android")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.firebase:firebase-messaging:23.0.0")
    implementation ("com.google.firebase:firebase-inappmessaging-display")
    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.github.getActivity:XXPermissions:20.0")
    implementation (platform("com.google.firebase:firebase-bom:32.8.0"))
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

//    // Kotlin 测试依赖
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.8.10")
//    testImplementation("junit:junit:4.13.2")
//
//    // Mocking 库
//    testImplementation("io.mockk:mockk:1.12.0")
//
//    // Compose 测试相关
//    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.3.0")
//    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.3.0")
//
//    // Coroutine 测试库
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
//
//    // AndroidX 测试库（UI 测试、JUnit4）
//    androidTestImplementation("androidx.test.ext:junit:1.1.4")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
//    androidTestImplementation("androidx.test:core:1.5.0")


}

android {
    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

configurations {
    all {
        exclude("com.google.guava.listenablefuture")
    }
}