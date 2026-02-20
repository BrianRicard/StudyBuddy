plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "studybuddy.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "studybuddy.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "studybuddy.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "studybuddy.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "studybuddy.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "studybuddy.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("jvmTest") {
            id = "studybuddy.jvm.test"
            implementationClass = "JvmTestConventionPlugin"
        }
    }
}
