// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

/** Map this to an immutable set via the given [transform]. */
fun <T, R> Iterable<T>.mapToSet(transform: (T) -> R): Set<R> = mapTo(mutableSetOf(), transform)
