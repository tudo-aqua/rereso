// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso

import com.charleskorn.kaml.Yaml
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import tools.aqua.rereso.testutil.circularLoadingYieldsEqualObject
import tools.aqua.rereso.testutil.serializedFormConformsToSchemas

internal class ToolSchemaTest {

  companion object {
    @JvmStatic val tools = listOf(SIMPLE_TOOL, COMPLEX_TOOL)
  }

  @ParameterizedTest(name = "[{index}]")
  @FieldSource("tools")
  fun `tool validates against schema`(tool: Tool) =
      tool.serializedFormConformsToSchemas(Yaml.default, ReReSoSchemas.tool)

  @ParameterizedTest(name = "[{index}]")
  @FieldSource("tools")
  fun `tool circular loading yields equal objects`(tool: Tool) =
      tool.circularLoadingYieldsEqualObject(Yaml.default)
}
