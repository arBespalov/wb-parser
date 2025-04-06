import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("androidx.navigation.safeargs")
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp")
}

ktlint {
    android.set(true)
    disabledRules.add("no-wildcard-imports")
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.automotivecodelab.wbgoodstracker"
        minSdk = 21
        targetSdk = 35
        versionCode = 8
        versionName = "1.3.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String", "SERVER_URL",
            gradleLocalProperties(rootDir, providers).getProperty("SERVER_URL")
        )
        buildConfigField(
            "String", "SERVER_CLIENT_ID",
            gradleLocalProperties(rootDir, providers).getProperty("SERVER_CLIENT_ID")
        )
        buildConfigField(
            "String", "USER_ID_FOR_DEBUG",
            gradleLocalProperties(rootDir, providers).getProperty("USER_ID_FOR_DEBUG")
        )
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }

        buildFeatures {
            buildConfig = true
        }

        // uncomment to run ktlint. It also shows kotlin packages as java ones
//        sourceSets {
//            val kotlinAdditionalSourceSets = project.file("src/main/kotlin")
//            val kotlinAdditionalSourceSetsTest = project.file("src/test/kotlin")
//            val kotlinAdditionalSourceSetsAndroidTest = project.file("src/androidTest/kotlin")
//            findByName("main")?.java?.srcDirs(
//                kotlinAdditionalSourceSets,
//                kotlinAdditionalSourceSetsTest,
//                kotlinAdditionalSourceSetsAndroidTest
//            )
//        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // offline release build
//            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
//                mappingFileUploadEnabled = false
//            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    dataBinding {
        enable = true
    }
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
    namespace = "com.automotivecodelab.wbgoodstracker"
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // ===retrofit===
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // ===room===
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // ===navigation===
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // ===dagger===
    implementation("com.google.dagger:dagger:2.55")
    ksp("com.google.dagger:dagger-compiler:2.55")

    // ===testing===
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // ===for in-app-review===
    implementation("com.google.android.play:review-ktx:2.0.2")

    // ===crashlytics===
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
