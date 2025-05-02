// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

import tools.aqua.rereso.util.CamelCaseSplitter.State.EMPTY
import tools.aqua.rereso.util.CamelCaseSplitter.State.NORMAL_WORD
import tools.aqua.rereso.util.CamelCaseSplitter.State.SINGLE_UPPER
import tools.aqua.rereso.util.CamelCaseSplitter.State.UPPER_WORD

/** If `this` is not empty, extend this with the given [prefix] and [suffix], else return `null`. */
fun String.ifNotEmpty(prefix: String = "", suffix: String = ""): String? =
    if (isNotEmpty()) "$prefix$this$suffix" else null

/**
 * State manager for splitting a String in camel case into its components.
 *
 * This is intended for Java's and Kotlin's default class and field naming conventions. Two types of
 * string parts are recognized: all-caps and regular camel case. An all-caps word ends at the
 * second-to-last capital letter in sequence. This ensures that, e.g., `"URLTokenizer"` is split
 * into `["URL", "Tokenizer"]`. Regular camel case words end at the last non-capital letter. The
 * first and last characters encountered always begin and end words, respectively.
 */
class CamelCaseSplitter {
  private enum class State {
    EMPTY,
    SINGLE_UPPER,
    UPPER_WORD,
    NORMAL_WORD
  }

  private val completeWords = mutableListOf<String>()
  private val builder = StringBuilder()
  private var state = EMPTY

  /** Add a new character [char] to the word. This advances the internal state. */
  operator fun plusAssign(char: Char) {
    when (state) {
      EMPTY -> {
        builder.append(char)
        state = if (char.isUpperCase()) SINGLE_UPPER else NORMAL_WORD
      }
      SINGLE_UPPER -> {
        builder.append(char)
        state = if (char.isUpperCase()) UPPER_WORD else NORMAL_WORD
      }
      UPPER_WORD -> {
        if (!char.isUpperCase()) {
          val startChar = builder.last()
          completeWords += builder.substring(0, builder.length - 1)
          builder.clear()
          builder.append(startChar)
          state = NORMAL_WORD
        }
        builder.append(char)
      }
      NORMAL_WORD -> {
        if (char.isUpperCase()) {
          completeWords += builder.toString()
          builder.clear()
          state = SINGLE_UPPER
        }
        builder.append(char)
      }
    }
  }

  /** Add all the characters in [string]. */
  operator fun plusAssign(string: String) = string.forEach { this += it }

  /**
   * Get the current splitting assumption.
   *
   * Note that the split may be revised according to new characters! E.g., `"URLT"` splits to
   * `["URLT"]`, but `"URLTo"` to `["URL", "To"]`.
   */
  val words: List<String>
    get() = completeWords + builder.toString()
}

/** Split this string into its camel-case components using [CamelCaseSplitter]. */
fun String.splitCamelCase(): List<String> = CamelCaseSplitter().also { it += this }.words

/**
 * Split this string into its camel-case components using [CamelCaseSplitter], lowercase them and
 * rejoin them in snake case using the [snake] character.
 */
fun String.camelToSnakeCase(snake: String = "_"): String =
    splitCamelCase().joinToString(snake) { it.lowercase() }
