// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalSerializationApi::class)

package tools.aqua.rereso

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tools.aqua.rereso.util.NullifiedEmptySet
import tools.aqua.rereso.util.implies

/**
 * A ReReSo Benchmark Set information object. This should be serialized as a top-level `rereso.yml`
 * file in a benchmark set distribution.
 *
 * @property metadata the generic ReReSo metadata for the benchmark.
 * @param license the default license for all benchmarks, can be `null` if all benchmarks have
 *   custom licenses.
 * @property format the data format used by the benchmarks in this set.
 * @property benchmarks the benchmarks in this set, must not be empty.
 */
@Serializable
data class BenchmarkSet(
    val metadata: Metadata,
    @EncodeDefault(NEVER) private val license: License? = null,
    val format: DataFormat,
    val benchmarks: NullifiedEmptySet<Benchmark>
) {
  /** The benchmark format version. Must be [ReReSoSchemas.version] at the moment. */
  @Required
  @SerialName("rereso-benchmark-version")
  val reresoVersion: String = ReReSoSchemas.version

  init {
    require((license == null) implies benchmarks.all { it.license != null }) {
      "either, a global license must be defined, or all benchmarks must define one"
    }
    require(benchmarks.isNotEmpty()) { "at least one benchmark is required" }
  }
}

/**
 * A single benchmark in a [BenchmarkSet].
 *
 * @property path the path to the benchmark relative to the benchmark root, in UNIX notation.
 * @property license the license for this individual benchmark. `null` indicates the default
 *   license.
 */
@Serializable
data class Benchmark(val path: String, @EncodeDefault(NEVER) val license: License? = null)
