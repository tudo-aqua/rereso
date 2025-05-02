// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RangesTest {

  @Test
  fun `disjoint ranges do not contain each other`() {
    assertThat(0..5 in 10..15).isFalse()
  }

  @Test
  fun `overlapping ranges do not contain each other`() {
    assertThat(0..5 in 3..8).isFalse()
  }

  @Test
  fun `outer ranges do not contain inner ranges`() {
    assertThat(0..5 in 2..3).isFalse()
  }

  @Test
  fun `inner ranges contain outer ranges`() {
    assertThat(2..3 in 0..5).isTrue()
  }

  @Test
  fun `disjoint ranges do not intersect each other`() {
    assertThat(0..5 intersects 10..15).isFalse()
  }

  @Test
  fun `overlapping ranges intersect each other`() {
    assertThat(0..5 intersects 3..8).isTrue()
  }

  @Test
  fun `outer ranges intersect inner ranges`() {
    assertThat(0..5 intersects 2..3).isTrue()
  }

  @Test
  fun `inner ranges intersect outer ranges`() {
    assertThat(2..3 intersects 0..5).isTrue()
  }

  @Test
  fun `disjoint ranges do not overlap each other`() {
    assertThat(0..5 overlaps 10..15).isFalse()
  }

  @Test
  fun `overlapping ranges overlap each other`() {
    assertThat(0..5 overlaps 3..8).isTrue()
  }

  @Test
  fun `outer ranges do not overlap inner ranges`() {
    assertThat(0..5 overlaps 2..3).isFalse()
  }

  @Test
  fun `inner ranges overlap outer ranges`() {
    assertThat(2..3 overlaps 0..5).isTrue()
  }
}
