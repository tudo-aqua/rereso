// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso

import jakarta.activation.MimeType
import java.net.URI

internal val SIMPLE_BENCHMARK =
    BenchmarkSet(
        Metadata("Simple Benchmark"),
        SpdxLicense("BSD"),
        DataFormat(MimeType("text", "plain")),
        setOf(Benchmark("example.txt")))

internal val OVERRIDDEN_LICENSE =
    BenchmarkSet(
        Metadata("Overridden License Benchmark"),
        SpdxLicense("BSD"),
        DataFormat(MimeType("text", "plain")),
        setOf(Benchmark("example.txt", SpdxLicense("CC-BY-4.0"))))

internal val ONLY_FILE_LICENSES =
    BenchmarkSet(
        Metadata("Only File Licenses Benchmark"),
        license = null,
        DataFormat(MimeType("text", "plain")),
        setOf(
            Benchmark(path = "example.txt", license = CustomLicense("All rights reserved")),
            Benchmark(path = "example2.txt", license = SpdxLicense("BSD"))))

internal val COMPLEX_BENCHMARK =
    BenchmarkSet(
        Metadata(
            name = "Complex Benchmark",
            description = "A Benchmark with multiple,\ncomplex features.",
            references = listOf(URI("https://aqua.tools"), URI("https://aqua.engineering"))),
        SpdxLicense("BSD"),
        DataFormat(
            MimeType("application", "json"),
            setOf(URI("https://aqua.tools/schema1.json"), URI("https://aqua.tools/schema2.json"))),
        setOf(
            Benchmark(path = "example.json", license = CustomLicense("All rights reserved")),
            Benchmark(path = "example2.json")))
