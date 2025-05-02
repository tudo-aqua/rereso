// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

/** True iff `this` => [other]. */
infix fun Boolean.implies(other: Boolean): Boolean = !this || other
