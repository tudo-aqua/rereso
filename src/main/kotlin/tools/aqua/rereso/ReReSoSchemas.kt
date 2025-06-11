// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso

/** The schemas for ReReSo. */
object ReReSoSchemas : SchemaGroup(yamlValidator()) {

  /** The ReReSo metadata version implemented by this library. */
  override val version: String = "0.3"

  init {
    registerBundledSchema("common")
  }

  /** The benchmark schema. */
  val benchmarkSet: Schema by registeringBundledSchema

  /** The tool schema. */
  val tool: Schema by registeringBundledSchema
}
