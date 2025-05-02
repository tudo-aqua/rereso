// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

/** True if and only if [other] is fully contained in `this`. */
infix operator fun <T : Comparable<T>> ClosedRange<T>.contains(other: ClosedRange<T>): Boolean =
    contains(other.start) && contains(other.endInclusive)

/**
 * True if and only if [other] and `this` at least partially overlap, i.e. have some values in
 * common.
 */
infix fun <T : Comparable<T>> ClosedRange<T>.intersects(other: ClosedRange<T>): Boolean =
    this in other || contains(other.start) || contains(other.endInclusive)

/**
 * True if and only if [other] and `this` only partially overlap, i.e. have some values in common
 * and some not in common.
 */
infix fun <T : Comparable<T>> ClosedRange<T>.overlaps(other: ClosedRange<T>): Boolean =
    this intersects other && other !in this
