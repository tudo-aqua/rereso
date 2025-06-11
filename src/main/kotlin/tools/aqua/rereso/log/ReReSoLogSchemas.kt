// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.log

import tools.aqua.rereso.Schema
import tools.aqua.rereso.SchemaGroup
import tools.aqua.rereso.jsonValidator

/** The schemas for ReReSo log benchmarks. */
object ReReSoLogSchemas : SchemaGroup(jsonValidator()) {

  /** The ReReSo log version implemented by this library. */
  override val version: String = "0.3"

  /** The log base schema. */
  val logs: Schema by registeringBundledSchema
  /** The classified log facet schema. */
  val classifiedLogs: Schema by registeringBundledSchema
  /** The normalized log facet schema. */
  val normalizedLogs: Schema by registeringBundledSchema
  /** The timed log facet schema. */
  val timedLogs: Schema by registeringBundledSchema
  /** The pre-split log facet schema. */
  val tvtSplitLogs: Schema by registeringBundledSchema
}
