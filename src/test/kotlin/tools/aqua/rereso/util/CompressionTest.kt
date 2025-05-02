// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class CompressionTest {

  companion object {

    private val codecs: List<Pair<(OutputStream) -> OutputStream, (InputStream) -> InputStream>> =
        listOf(
            { os: OutputStream -> os.gzip() } to { os: InputStream -> os.gunzip() },
            { os: OutputStream -> os.bzip2() } to { os: InputStream -> os.bunzip2() },
            { os: OutputStream -> os.xz() } to { os: InputStream -> os.unxz() },
            { os: OutputStream -> os.zstd() } to { os: InputStream -> os.unzstd() },
        )

    private val strings = RandomStringUtils.insecure()

    @JvmStatic
    fun compressionCases(): Iterable<Arguments> =
        sequence {
              codecs.forEach { (encoder, decoder) ->
                listOf(10, 100, 1000).forEach { size ->
                  yield(arguments(strings.next(size), encoder, decoder))
                }
              }
            }
            .asIterable()
  }

  @ParameterizedTest
  @MethodSource("compressionCases")
  fun `check circular compression and decompression`(
      string: String,
      encoder: (OutputStream) -> OutputStream,
      decoder: (InputStream) -> InputStream
  ) {
    val buffer = ByteArrayOutputStream()

    encoder(buffer).bufferedWriter(Charsets.UTF_8).use { it.write(string) }

    val decoded =
        decoder(ByteArrayInputStream(buffer.toByteArray())).bufferedReader(Charsets.UTF_8).use {
          it.readText()
        }

    assertThat(decoded).isEqualTo(string)
  }
}
