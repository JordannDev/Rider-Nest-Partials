import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask

plugins {
    kotlin("jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = "com.jordanmolenaar.rider.partialnesting"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        jetbrainsRuntime()
    }
}

dependencies {
    intellijPlatform {
        val riderHome = providers.gradleProperty("riderHome")
        if (riderHome.isPresent) {
            local(riderHome.get())
        } else {
            rider(providers.gradleProperty("riderVersion").get()) { useInstaller = false }
            jetbrainsRuntime()
        }
        bundledModule("intellij.rider.rdclient.dotnet")
    }
}

kotlin {
    jvmToolchain(21)
    sourceSets.main {
        kotlin.srcDirs("src/rider/main/kotlin", "protocol/build/generated/kotlin")
    }
}

sourceSets.main { resources.srcDir("src/rider/main/resources") }

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "262.10968.170"
            untilBuild = "262.*"
        }
    }
    pluginVerification {
        ides { current() }
    }
}

tasks.compileKotlin { dependsOn(":protocol:rdgen") }

val buildBackend by tasks.registering(Exec::class) {
    dependsOn(":protocol:rdgen")
    commandLine("dotnet", "build", "src/dotnet/PartialNesting/PartialNesting.csproj", "-c", "Release")
}

val testBackend by tasks.registering(Exec::class) {
    commandLine("dotnet", "test", "src/dotnet/PartialNesting.Tests/PartialNesting.Tests.csproj", "-c", "Release")
}

tasks.check { dependsOn(testBackend) }

tasks.withType<PrepareSandboxTask>().configureEach {
    dependsOn(buildBackend)
    from("src/dotnet/PartialNesting/bin/Release/net472") {
        include("PartialNesting.dll", "PartialNesting.pdb")
        into("${rootProject.name}/dotnet")
    }
}

tasks.runIde {
    maxHeapSize = "2g"
    args(providers.gradleProperty("sandboxSolution")
        .orElse(file("samples/PartialClasses/PartialClasses.sln").absolutePath).get())
}
