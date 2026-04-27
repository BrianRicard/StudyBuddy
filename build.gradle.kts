plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.versions)
    alias(libs.plugins.dependency.check)
    alias(libs.plugins.licensee) apply false
}

kover {
    merge {
        allProjects()
    }
}

detekt {
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
    parallel = true
}

dependencyCheck {
    formats = listOf("HTML", "JSON", "SARIF")
    failBuildOnCVSS = 7.0f
    suppressionFile = "$rootDir/owasp-suppressions.xml"

    // Persist the NVD cache to a stable, cacheable location across CI runs.
    data.directory = "$rootDir/.gradle/dependency-check-data"

    // Disable analyzers irrelevant to an Android/Kotlin project to speed up scans
    // and reduce false positives.
    analyzers.assemblyEnabled = false
    analyzers.nuspecEnabled = false
    analyzers.nugetconfEnabled = false
    analyzers.nodeEnabled = false
    analyzers.nodeAuditEnabled = false
    analyzers.composerEnabled = false
    analyzers.cpanEnabled = false
    analyzers.cocoapodsEnabled = false
    analyzers.swiftEnabled = false
    analyzers.bundleAuditEnabled = false
    analyzers.golangDepEnabled = false
    analyzers.golangModEnabled = false
    analyzers.rubygemsEnabled = false

    nvd.apiKey = System.getenv("NVD_API_KEY") ?: ""
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    ktlint {
        android.set(true)
        verbose.set(true)
    }
}
