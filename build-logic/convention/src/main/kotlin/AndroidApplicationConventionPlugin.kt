import com.android.build.api.dsl.ApplicationExtension
import java.io.ByteArrayOutputStream
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            val gitVersionCode = gitCommitCount()
            val gitVersionName = gitDescribeTag()

            extensions.configure<ApplicationExtension> {
                compileSdk = 35

                defaultConfig {
                    minSdk = 26
                    targetSdk = 35
                    versionCode = gitVersionCode
                    versionName = gitVersionName
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                lint {
                    lintConfig = rootProject.file("lint.xml")
                    xmlReport = true
                    htmlReport = true
                    sarifReport = true
                    checkDependencies = true
                }

                compileOptions {
                    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
                    targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
                }

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add("androidTestImplementation", libs.findLibrary("android-test-runner").get())
                add("androidTestImplementation", libs.findLibrary("android-test-ext-junit").get())
            }
        }
    }

    /**
     * Returns the total number of git commits, used as an auto-incrementing versionCode.
     * Falls back to 1 if git is unavailable (e.g. in a CI archive without .git).
     */
    private fun Project.gitCommitCount(): Int {
        return try {
            val stdout = ByteArrayOutputStream()
            exec { spec ->
                spec.commandLine("git", "rev-list", "--count", "HEAD")
                spec.standardOutput = stdout
            }
            stdout.toString().trim().toIntOrNull() ?: 1
        } catch (_: Exception) {
            1
        }
    }

    /**
     * Returns the most recent git tag (cleaned), used as versionName.
     * Falls back to "1.0.0-dev" if no tags exist or git is unavailable.
     */
    private fun Project.gitDescribeTag(): String {
        return try {
            val stdout = ByteArrayOutputStream()
            exec { spec ->
                spec.commandLine("git", "describe", "--tags", "--abbrev=0")
                spec.standardOutput = stdout
            }
            val tag = stdout.toString().trim()
            tag.removePrefix("v").ifBlank { "1.0.0-dev" }
        } catch (_: Exception) {
            "1.0.0-dev"
        }
    }
}
