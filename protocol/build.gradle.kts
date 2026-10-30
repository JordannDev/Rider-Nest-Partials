import com.jetbrains.rd.generator.gradle.RdGenTask

plugins {
    kotlin("jvm") version "2.3.20"
    id("com.jetbrains.rdgen") version "2026.2.5"
}

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.3.20")
    implementation("com.jetbrains.rd:rd-gen:2026.2.5")
}

kotlin { jvmToolchain(21) }

rdgen {
    packages = "model"
    generator {
        language = "kotlin"
        transform = "asis"
        root = "model.PartialNestingRoot"
        namespace = "cloud.jordanm.rider.partialnesting.protocol"
        directory = layout.buildDirectory.dir("generated/kotlin").get().asFile.path
    }
    generator {
        language = "csharp"
        transform = "reversed"
        root = "model.PartialNestingRoot"
        namespace = "PartialNesting.Protocol"
        directory = layout.buildDirectory.dir("generated/csharp").get().asFile.path
    }
}

tasks.withType<RdGenTask> {
    val modelClasspath = sourceSets.main.get().runtimeClasspath
    dependsOn(modelClasspath)
    classpath(modelClasspath)
}
