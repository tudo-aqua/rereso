// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CamelCaseSplitterTest {

  @Test
  fun `empty word is retained`() {
    assertThat("".splitCamelCase()).containsExactly("")
  }

  @Test
  fun `lowercase word is retained`() {
    println("word".splitCamelCase())
    assertThat("word".splitCamelCase()).containsExactly("word")
  }

  @Test
  fun `title case word is retained`() {
    assertThat("Word".splitCamelCase()).containsExactly("Word")
  }

  @Test
  fun `mixed camel case words are split`() {
    assertThat("twoWords".splitCamelCase()).containsExactly("two", "Words")
  }

  @Test
  fun `camel case words are split`() {
    assertThat("TwoWords".splitCamelCase()).containsExactly("Two", "Words")
  }

  @Test
  fun `uppercase word is not split`() {
    assertThat("WORD".splitCamelCase()).containsExactly("WORD")
  }

  @Test
  fun `uppercase first word is not split`() {
    assertThat("TWOWords".splitCamelCase()).containsExactly("TWO", "Words")
  }

  @Test
  fun `uppercase intermediate word is not split`() {
    assertThat("ThreeDIFFERENTWords".splitCamelCase())
        .containsExactly("Three", "DIFFERENT", "Words")
  }

  @Test
  fun `uppercase final word is not split`() {
    assertThat("twoWORDS".splitCamelCase()).containsExactly("two", "WORDS")
  }
}
