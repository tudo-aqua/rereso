// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

import org.apache.tools.ant.DirectoryScanner

plugins {
  id("com.gradle.develocity") version "4.0.2"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "rereso"

DirectoryScanner.removeDefaultExclude("**/.gitignore")

develocity {
  buildScan {
    val isCI = System.getenv("CI").isNullOrEmpty().not()
    publishing.onlyIf { isCI }
    if (isCI) {
      tag("CI")
      uploadInBackground = false
      termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
      termsOfUseAgree = "yes"
    }
  }
}
