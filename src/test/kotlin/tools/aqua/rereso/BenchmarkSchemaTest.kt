// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso

import com.charleskorn.kaml.Yaml
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import tools.aqua.rereso.testutil.circularLoadingYieldsEqualObject
import tools.aqua.rereso.testutil.serializedFormConformsToSchemas

internal class BenchmarkSchemaTest {

  companion object {
    @JvmStatic
    val benchmarks =
        listOf(SIMPLE_BENCHMARK, OVERRIDDEN_LICENSE, ONLY_FILE_LICENSES, COMPLEX_BENCHMARK)
  }

  @ParameterizedTest(name = "[{index}]")
  @FieldSource("benchmarks")
  fun `benchmark validates against schema`(benchmark: BenchmarkSet) =
      benchmark.serializedFormConformsToSchemas(Yaml.default, ReReSoSchemas.benchmarkSet)

  @ParameterizedTest(name = "[{index}]")
  @FieldSource("benchmarks")
  fun `benchmark circular loading yields equal objects`(benchmarkSet: BenchmarkSet) =
      benchmarkSet.circularLoadingYieldsEqualObject(Yaml.default)
}
