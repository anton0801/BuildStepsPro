plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.buildSteps.BuildStepsPro"
    compileSdk = 36

    signingConfigs {
        create("release") {
            val isCI = "true" == System.getenv("CI")

            if (isCI) {
                val keystorePath = System.getenv("CM_KEYSTORE_PATH")
                    ?: throw GradleException("CM_KEYSTORE_PATH не установлен в CI!")

                storeFile = file(keystorePath)
                storePassword = System.getenv("CM_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("CM_KEY_ALIAS")
                keyPassword = System.getenv("CM_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "com.buildSteps.BuildStepsPro"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
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
        viewBinding = true
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
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.material.icons)
    debugImplementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.gson)
    debugImplementation(libs.accompanist.systemuicontroller)
    debugImplementation(libs.lottie.compose)


    implementation(libs.androidx.navigation.fragment)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.7")


    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation(libs.androidx.animation)
    implementation(libs.androidx.foundation)

    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("com.google.accompanist:accompanist-pager:0.36.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.36.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.7")
    // implementation("com.google.code.gson:gson:2.13.2")

    implementation(libs.installreferrer)
    implementation(libs.appsflayer)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation (project.dependencies.platform(libs.firebase.bom))
    implementation (libs.firebase.analytics.ktx)
    implementation (libs.firebase.messaging.ktx)

    implementation (libs.play.services.ads.identifier)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.kotlinx.serialization)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.constraintlayout)
}