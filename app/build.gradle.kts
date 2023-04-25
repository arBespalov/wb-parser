import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs")
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

ktlint {
    android.set(true)
    disabledRules.add("no-wildcard-imports")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.automotivecodelab.wbgoodstracker"
        minSdk = 21
        targetSdk = 33
        versionCode = 7
        versionName = "1.3.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String", "SERVER_URL",
            gradleLocalProperties(rootDir).getProperty("SERVER_URL")
        )
        buildConfigField(
            "String", "SERVER_CLIENT_ID",
            gradleLocalProperties(rootDir).getProperty("SERVER_CLIENT_ID")
        )
        buildConfigField(
            "String", "USER_ID_FOR_DEBUG",
            gradleLocalProperties(rootDir).getProperty("USER_ID_FOR_DEBUG")
        )
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
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
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("com.google.android.gms:play-services-auth:20.4.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")

    // ===retrofit===
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ===room===
    implementation("androidx.room:room-runtime:2.5.0")
    annotationProcessor("androidx.room:room-compiler:2.5.0")
    kapt("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")

    // ===navigation===
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")

    // ===dagger===
    implementation("com.google.dagger:dagger:2.45")
    kapt("com.google.dagger:dagger-compiler:2.45")

    // ===testing===
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")
    androidTestImplementation("androidx.room:room-testing:2.5.0")

    // ===for in-app-review===
    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.play:core-ktx:1.8.1")

    // ===crashlytics===
    implementation(platform("com.google.firebase:firebase-bom:29.3.1"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
