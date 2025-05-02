// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso

import jakarta.activation.MimeType
import java.net.URI

internal val SIMPLE_TOOL =
    Tool(
        Metadata("Simple Tool"),
        SpdxLicense("BSD"),
        setOf(
            ToolCommand(
                "default",
                DataFormat(MimeType("text", "plain")),
                listOf(DataFormat(MimeType("text", "plain"))))),
        "tool.sif")

internal val COMPLEX_TOOL =
    Tool(
        Metadata(
            name = "Complex Tool",
            description = "A tool with multiple,\ncomplex features.",
            references = listOf(URI("https://aqua.tools"), URI("https://aqua.engineering"))),
        CustomLicense("All rights reserved"),
        setOf(
            ToolCommand(
                name = "operation <in> -l <log> -o <out>",
                inputFormat = DataFormat(MimeType("text", "csv")),
                outputFormats =
                    listOf(
                        DataFormat(
                            MimeType("text", "plain"),
                        ),
                        DataFormat(
                            MimeType("application", "json"),
                            setOf(URI("https://aqua.tools/schema1.json"))))),
            ToolCommand(
                name = "otherOperation <in> -o <out>",
                inputFormat = DataFormat(MimeType("text", "csv")),
                outputFormats =
                    listOf(
                        DataFormat(
                            MimeType("application", "xml"),
                            setOf(
                                URI("https://aqua.tools/schema1.json"),
                                URI("https://aqua.tools/schema2.json")))))),
        "tool.sif")
