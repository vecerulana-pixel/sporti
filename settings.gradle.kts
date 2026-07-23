pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Sporti"

include(
    ":app",
    ":core:domain",
    ":core:data",
    ":core:designsystem",
    ":feature:home",
    ":feature:explore",
    ":feature:library",
    ":feature:analytics",
)
