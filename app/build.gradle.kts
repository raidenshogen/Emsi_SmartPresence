plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.emsismartpresence"
    compileSdk = 35  // Changed from 35 to 34

    defaultConfig {
        applicationId = "com.example.emsismartpresence"
        minSdk = 29
        targetSdk = 34  // Changed from 35 to 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core dependencies
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.material.v1110)
    implementation(libs.androidx.activity.v182)
    implementation(libs.androidx.constraintlayout.v214)
    implementation(libs.androidx.navigation.fragment.v276)
    implementation(libs.androidx.navigation.ui.v276)

    // Maps and location
    implementation(libs.gms.play.services.maps.v1820)
    implementation(libs.play.services.location.v2101)

    // ... existing dependencies ...

//    implementation(libs.play.services.location)

    // ... existing dependencies ...
    implementation("com.google.android.gms:play-services-maps:19.1.0")



    // Firebase
    implementation(platform(libs.firebase.bom.v3270))
    implementation(libs.google.firebase.analytics)
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.ui.auth.v720)

//     UI components
    implementation(libs.androidx.recyclerview.v132)
    implementation(libs.cardview)
    implementation(libs.hdodenhof.circleimageview)
    implementation(libs.androidx.annotation)

    // Image loading
    implementation(libs.glide)
    implementation(libs.androidx.tools.core)
    implementation(libs.volley)
    annotationProcessor(libs.compiler)

    // PDF Generation
    implementation(libs.itext7.core)

//    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)

    //Ai
    implementation(libs.okhttp)
    implementation(libs.json.json)

    //sheet
//   implementation ("com.google.api-client:google-api-client-android:1.32.1")
    implementation ("com.google.android.material:material:1.9.0")
}

