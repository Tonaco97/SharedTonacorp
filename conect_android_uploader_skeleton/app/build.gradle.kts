plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "br.com.conect.uploader"
  compileSdk = 34
  defaultConfig {
    applicationId = "br.com.conect.uploader"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
  }
  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.activity:activity-ktx:1.9.2")
}
