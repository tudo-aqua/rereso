// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalSerializationApi::class)

package tools.aqua.rereso.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/** A mostly-compliant JSON5 format. */
val json5: Json = Json {
  allowComments = true
  allowTrailingComma = true
  isLenient = true
}
