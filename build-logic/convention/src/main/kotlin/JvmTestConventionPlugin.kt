import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class JvmTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType(
                org.gradle.api.artifacts.VersionCatalogsExtension::class.java,
            ).named("libs")

            tasks.withType<Test> {
                useJUnitPlatform()
            }

            dependencies {
                add("testImplementation", libs.findBundle("testing").get())
                add("testRuntimeOnly", libs.findLibrary("junit5-engine").get())
            }
        }
    }
}
