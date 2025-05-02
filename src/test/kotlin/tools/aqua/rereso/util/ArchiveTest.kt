// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.text.Charsets.UTF_8
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ArchiveTest {

  companion object {
    private val strings = RandomStringUtils.insecure()
    val string1 = strings.next(1000)
    val string2 = strings.next(1000)
  }

  @Test
  fun `check circular tar operations work`() {
    val buffer = ByteArrayOutputStream()

    buffer.tar().use {
      val data1 = string1.toByteArray()
      it.putArchiveEntry(TarArchiveEntry("test/file.txt").also { it.size = data1.size.toLong() })
      it.write(data1)
      it.closeArchiveEntry()

      val data2 = string2.toByteArray()
      it.putArchiveEntry(TarArchiveEntry("test/other.txt").also { it.size = data2.size.toLong() })
      it.write(data2)
      it.closeArchiveEntry()
    }

    ByteArrayInputStream(buffer.toByteArray()).untar().use {
      val entry1 = it.nextEntry
      assertThat(entry1).extracting { it.name }.isEqualTo("test/file.txt")
      val content1 = it.readNBytes(entry1.size.toInt()).toString(UTF_8)
      assertThat(content1).isEqualTo(string1)

      val entry2 = it.nextEntry
      assertThat(entry2).extracting { it.name }.isEqualTo("test/other.txt")
      val content2 = it.readNBytes(entry2.size.toInt()).toString(UTF_8)
      assertThat(content2).isEqualTo(string2)
    }
  }
}
