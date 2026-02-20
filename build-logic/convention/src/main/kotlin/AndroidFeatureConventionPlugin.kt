import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("studybuddy.android.library")
                apply("studybuddy.android.compose")
                apply("studybuddy.android.hilt")
                apply("studybuddy.jvm.test")
            }

            val libs = extensions.getByType(
                org.gradle.api.artifacts.VersionCatalogsExtension::class.java
            ).named("libs")

            dependencies {
                add("implementation", libs.findLibrary("navigation-compose").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("implementation", libs.findBundle("lifecycle").get())
            }
        }
    }
}
