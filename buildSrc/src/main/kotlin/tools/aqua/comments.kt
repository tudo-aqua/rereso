// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua

fun String.asCFlowerboxComment(): String =
    lines().joinToString("\n", prefix = "/*\n", postfix = "\n */\n\n") { " * $it" }

fun ReuseHeader.asCFlowerboxComment(): String = toString().asCFlowerboxComment()

fun String.asCppComments(): String = lines().joinToString("\n", postfix = "\n\n") { "// $it" }

fun ReuseHeader.asCppComments(): String = toString().asCppComments()

fun String.asHashmarkComments(): String = lines().joinToString("\n", postfix = "\n\n") { "# $it" }

fun ReuseHeader.asHashmarkComments(): String = toString().asHashmarkComments()

fun String.asXmlComment(): String =
    lines().joinToString("\n", prefix = "<!--\n", postfix = "\n  -->\n\n") { "  $it" }

fun ReuseHeader.asXmlComment(): String = toString().asXmlComment()
