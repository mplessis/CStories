import org.gradle.api.publish.PublishingExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.hot.reload) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    group = property("cstoriesGroup") as String
    version = property("cstoriesVersion") as String
}

subprojects {
    plugins.withId("maven-publish") {
        val githubUsername = providers.gradleProperty("gpr.user")
            .orElse(providers.environmentVariable("GITHUB_ACTOR"))
        val githubToken = providers.gradleProperty("gpr.token")
            .orElse(providers.environmentVariable("GITHUB_TOKEN"))

        extensions.configure(PublishingExtension::class.java) {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/mplessis/CStories")
                    credentials {
                        username = githubUsername.orNull ?: ""
                        password = githubToken.orNull ?: ""
                    }
                }
            }
        }
    }
}

/**
 * `cstories-gradle-plugin` is a separate, included Gradle build (see
 * `settings.gradle.kts`'s `pluginManagement { includeBuild(...) }`), so its
 * own `publishToMavenLocal` task isn't picked up by the root project's
 * `publishToMavenLocal` — only the regular subprojects
 * (`cstories-annotations`, `cstories-processor`, `cstories-runtime`) are.
 * This aggregate task publishes both, so a single command always keeps
 * every `dev.cstories:*` artifact (including the plugin marker) in sync in
 * `mavenLocal()` for consumer projects.
 */
tasks.register("publishAllToMavenLocal") {
    group = "publishing"
    description =
        "Publishes every dev.cstories artifact, including the cstories-gradle-plugin included build, to mavenLocal()"
    dependsOn(
        ":cstories-annotations:publishToMavenLocal",
        ":cstories-processor:publishToMavenLocal",
        ":cstories-runtime:publishToMavenLocal",
        gradle.includedBuild("cstories-gradle-plugin").task(":publishToMavenLocal"),
    )
}

tasks.register("publishAllToGitHubPackages") {
    group = "publishing"
    description = "Publishes every dev.cstories artifact, including the cstories-gradle-plugin included build, to GitHub Packages"
    dependsOn(
        ":cstories-annotations:publishAllPublicationsToGitHubPackagesRepository",
        ":cstories-processor:publishAllPublicationsToGitHubPackagesRepository",
        ":cstories-runtime:publishAllPublicationsToGitHubPackagesRepository",
        gradle.includedBuild("cstories-gradle-plugin").task(":publishAllPublicationsToGitHubPackagesRepository"),
    )
}
