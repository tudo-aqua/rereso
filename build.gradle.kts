// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

import com.diffplug.gradle.spotless.KotlinExtension
import com.diffplug.gradle.spotless.KotlinGradleExtension
import com.diffplug.gradle.spotless.SpotlessTask
import com.github.gradle.node.variant.computeNodeDir
import com.github.gradle.node.variant.computeNodeExec
import java.time.Year
import org.gradle.api.attributes.Bundling.BUNDLING_ATTRIBUTE
import org.gradle.api.attributes.Bundling.EXTERNAL
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.DOCUMENTATION
import org.gradle.api.attributes.DocsType.DOCS_TYPE_ATTRIBUTE
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.plugins.BasePlugin.BUILD_GROUP
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import tools.aqua.FileCopyright
import tools.aqua.License.APACHE_2_0
import tools.aqua.License.CC_BY_4_0
import tools.aqua.ReuseHeader
import tools.aqua.asCppComments
import tools.aqua.asHashmarkComments
import tools.aqua.asXmlComment

plugins {
  `java-library`
  `maven-publish`
  signing

  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka.html)
  alias(libs.plugins.dokka.javadoc)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.gitVersioning)
  alias(libs.plugins.moduleInfo)
  alias(libs.plugins.nexusPublish)
  alias(libs.plugins.node)
  alias(libs.plugins.spotless)
  alias(libs.plugins.taskTree)
  alias(libs.plugins.versions)
}

group = "tools.aqua"

version = "0.0.0-undetected-SNAPSHOT"

gitVersioning.apply {
  describeTagFirstParent = false
  refs {
    considerTagsOnBranches = true
    tag("(?<version>.*)") {
      // on a tag: use the tag name as is
      version = "\${ref.version}"
    }
    branch("main") {
      // on the main branch: use <last.tag.version>-<distance>-<commit>-SNAPSHOT
      version = "\${describe.tag.version}-\${describe.distance}-\${commit.short}-SNAPSHOT"
    }
    branch(".+") {
      // on other branches: use <last.tag.version>-<branch.name>-<distance>-<commit>-SNAPSHOT
      version =
          "\${describe.tag.version}-\${ref.slug}-\${describe.distance}-\${commit.short}-SNAPSHOT"
    }
  }

  // optional fallback configuration in case of no matching ref configuration
  rev {
    // in case of missing git data: use 0.0.0-unknown-0-<commit>-SNAPSHOT
    version = "0.0.0-unknown-0-\${commit.short}-SNAPSHOT"
  }
}

repositories { mavenCentral() }

dependencies {
  api(libs.activation)
  api(libs.commons.compress)
  api(libs.datetime)
  api(libs.jsonSchema)
  api(libs.serialization.json)
  api(libs.serialization.yaml)

  implementation(libs.csv)
  implementation(libs.matlab)
  implementation(libs.snakeyaml)
  implementation(libs.xz)
  implementation(libs.zstd)

  testImplementation(platform(libs.junit))
  testImplementation(libs.assertj.core)
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.launcher)

  detektPlugins(libs.detekt.compiler)
  detektPlugins(libs.detekt.faire)
}

node {
  download = true
  workDir = layout.buildDirectory.dir("nodejs")
}

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(version)
  return isStable.not()
}

tasks.dependencyUpdates {
  gradleReleaseChannel = "current"
  revision = "release"
  rejectVersionIf { isNonStable(candidate.version) && !isNonStable(currentVersion) }
}

spotless {
  val copyright = FileCopyright(Year.of(2023), "The ReReSo Authors, see AUTHORS.md")
  val apache20 = ReuseHeader(APACHE_2_0, copyright)
  val ccBy40 = ReuseHeader(CC_BY_4_0, copyright)

  format("json5") {
    target("src/*/resources/**/*.json5")
    licenseHeader(ccBy40.asCppComments(), """\{""")
    prettier()
        .npmInstallCache()
        .nodeExecutable(computeNodeExec(node, computeNodeDir(node)).get())
        .config(mapOf("parser" to "json5", "printWidth" to 100))
  }
  kotlin {
    licenseHeader(apache20.asCppComments())
    ktfmt()
  }
  format("kotlinBuildSrc", KotlinExtension::class.java) {
    target("buildSrc/src/*/kotlin/**/*.kt")
    licenseHeader(apache20.asCppComments())
    ktfmt()
  }
  kotlinGradle {
    licenseHeader(apache20.asCppComments(), "(plugins|import )")
    ktfmt()
  }
  format("kotlinGradleBuildSrc", KotlinGradleExtension::class.java) {
    target("buildSrc/*.gradle.kts")
    licenseHeader(
        apache20.asCppComments(),
        "(import |@file|plugins |dependencyResolutionManagement|rootProject.name)")
    ktfmt()
  }
  format("markdown") {
    target("*.md")
    licenseHeader(ccBy40.asXmlComment(), """(#+|\[!\[)""")
    prettier()
        .npmInstallCache()
        .nodeExecutable(computeNodeExec(node, computeNodeDir(node)).get())
        .config(mapOf("parser" to "markdown", "printWidth" to 100, "proseWrap" to "always"))
  }
  format("misc") {
    target(".gitignore")
    licenseHeader(apache20.asHashmarkComments(), """[.a-zA-Z0-9/*]+""")
    trimTrailingWhitespace()
    endWithNewline()
  }
  format("properties") {
    target("*.properties", "gradle/**/*.properties")
    licenseHeader(apache20.asHashmarkComments(), "[-.a-zA-Z0-9]+ *=")
    prettier(mapOf("prettier-plugin-properties" to libs.versions.prettier.properties.get()))
        .npmInstallCache()
        .nodeExecutable(computeNodeExec(node, computeNodeDir(node)).get())
        .config(
            mapOf(
                "plugins" to listOf("prettier-plugin-properties"),
                "parser" to "dot-properties",
                "printWidth" to 100,
            ))
  }
  format("toml") {
    target("gradle/libs.versions.toml", "REUSE.toml")
    licenseHeader(apache20.asHashmarkComments(), """(version|\[[A-Za-z-]+])""")
    prettier(mapOf("prettier-plugin-toml" to libs.versions.prettier.toml.get()))
        .npmInstallCache()
        .nodeExecutable(computeNodeExec(node, computeNodeDir(node)).get())
        .config(
            mapOf(
                "plugins" to listOf("prettier-plugin-toml"),
                "parser" to "toml",
                "alignComments" to false,
                "printWidth" to 100,
            ))
  }
  yaml {
    target(".github/**/*.yml")
    licenseHeader(apache20.asHashmarkComments(), "[A-Za-z-]+:")
    prettier()
        .npmInstallCache()
        .nodeExecutable(computeNodeExec(node, computeNodeDir(node)).get())
        .config(mapOf("parser" to "yaml", "printWidth" to 100))
  }
}

tasks.withType<SpotlessTask>().configureEach { dependsOn(tasks.npmSetup) }

detekt { config.setFrom(project.file("config/detekt.yml")) }

kotlin { jvmToolchain(17) }

val kdocJar by
    tasks.registering(Jar::class) {
      group = BUILD_GROUP
      dependsOn(tasks.dokkaGeneratePublicationHtml)
      from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
      archiveClassifier.set("kdoc")
    }

val kdoc =
    configurations.consumable("kdocElements") {
      isVisible = false

      attributes {
        attribute(CATEGORY_ATTRIBUTE, objects.named(DOCUMENTATION))
        attribute(USAGE_ATTRIBUTE, objects.named(JAVA_RUNTIME))
        attribute(BUNDLING_ATTRIBUTE, objects.named(EXTERNAL))
        attribute(DOCS_TYPE_ATTRIBUTE, objects.named("kdoc"))
      }

      outgoing.artifact(kdocJar)
    }

val javaComponent = components.findByName("java") as AdhocComponentWithVariants

javaComponent.addVariantsFromConfiguration(kdoc.get()) {}

val javadocJar by
    tasks.registering(Jar::class) {
      group = BUILD_GROUP
      dependsOn(tasks.dokkaGeneratePublicationJavadoc)
      from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
      archiveClassifier.set("javadoc")
    }

java {
  withJavadocJar()
  withSourcesJar()
}

tasks.test {
  useJUnitPlatform()
  testLogging { events(PASSED, SKIPPED, FAILED) }
}

val maven by
    publishing.publications.creating(MavenPublication::class) {
      from(components["java"])
      pom {
        name = "ReReSo"
        description = "Reusable Research Software: Data Types, Schemas, and Utilities"
        url = "https://aqua.tools/rereso"
        licenses {
          license {
            name = "Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
          }
          license {
            name = "Creative Commons Attribution 4.0 International"
            url = "https://creativecommons.org/licenses/by/4.0/"
          }
        }
        developers {
          developer {
            name = "Simon Dierl"
            email = "simon.dierl@tu-dortmund.de"
            organization = "AQUA Group, Department of Computer Science, TU Dortmund University"
            organizationUrl = "https://aqua.engineering/"
          }
        }
        scm {
          connection = "scm:git:https://github.com/tudo-aqua/rereso.git"
          developerConnection = "scm:git:ssh://git@github.com:tudo-aqua/rereso.git"
          url = "https://github.com/tudo-aqua/rereso/tree/main"
        }
      }
    }

signing {
  setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }
  useGpgCmd()
  sign(maven)
}

nexusPublishing { this.repositories { sonatype() } }
