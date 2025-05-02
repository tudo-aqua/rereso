// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.log

import java.time.Month.JULY
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone.Companion.UTC
import kotlinx.datetime.toInstant
import tools.aqua.rereso.log.Split.TEST
import tools.aqua.rereso.log.Split.VALIDATION

internal val SIMPLE_LOG_ARCHIVE = LogArchive(logs = setOf(Log(entries = listOf("a"))))

internal val CHAR_LOG_ARCHIVE =
    LogArchive("Char Entries", setOf(Log("Char Log", listOf("a", "b", "c"))))

internal val STRING_LOG = Log("String Log", listOf("abc", "def", "ghi"))

internal val STRING_LOG_ARCHIVE = LogArchive("String Entries", logs = setOf(STRING_LOG))

internal val CLASSIFIED_LOG = Log("Classified Log", listOf("abc", "def", "ghi"), classifier = "cls")

internal val CLASSIFIED_LOG_ARCHIVE = LogArchive("Classified Entries", logs = setOf(CLASSIFIED_LOG))

internal val PARTIALLY_CLASSIFIED_LOG_ARCHIVE =
    LogArchive("Partially Classified Entries", logs = setOf(CLASSIFIED_LOG, STRING_LOG))

internal val NORMALIZED_LOG =
    Log(
        "Normalized Log",
        listOf(
            LogEntry("abc", denormalized = "abc(x)", parameters = listOf("x")),
            LogEntry("def", denormalized = "def(x,y)", parameters = listOf("x", "y")),
            LogEntry("ghi", denormalized = "ghi", parameters = emptyList())))

internal val NORMALIZED_LOG_ARCHIVE = LogArchive("Normalized Entries", logs = setOf(NORMALIZED_LOG))

internal val PARTIALLY_NORMALIZED_LOG_ARCHIVE =
    LogArchive("Partially Normalized Entries", logs = setOf(NORMALIZED_LOG, STRING_LOG))

internal val SIMPLE_TIMED_LOG =
    Log(
        "Timed Log",
        listOf(
            LogEntry("abc", relativeStart = 0.seconds),
            LogEntry("def", relativeStart = 1.seconds),
            LogEntry("ghi", relativeStart = 2.seconds)))

internal val COMPLEX_TIMED_LOG =
    Log(
        "Timed Log",
        listOf(
            LogEntry("abc", relativeStart = 0.seconds, duration = 1.seconds),
            LogEntry("def", relativeStart = 2.seconds, duration = 5.seconds),
            LogEntry("ghi", relativeStart = 7.seconds, duration = 10.seconds)),
        epoch =
            LocalDateTime(year = 1969, month = JULY, dayOfMonth = 20, hour = 20, minute = 17)
                .toInstant(UTC),
        duration = 1.hours,
    )

internal val TIMED_LOG_ARCHIVE =
    LogArchive("Timed Entries", logs = setOf(SIMPLE_TIMED_LOG, COMPLEX_TIMED_LOG))

internal val PARTIALLY_TIMED_LOG_ARCHIVE =
    LogArchive(
        "Partially Timed Entries", logs = setOf(SIMPLE_TIMED_LOG, COMPLEX_TIMED_LOG, STRING_LOG))

internal val TEST_LOG = Log("String Log", listOf("jkl", "mno", "pqr"), split = TEST)
internal val VALIDATION_LOG = Log("String Log", listOf("stu", "vwx", "yz"), split = VALIDATION)

internal val TTV_SPLIT_LOG_ARCHIVE =
    LogArchive("TTV Split Entries", logs = setOf(STRING_LOG, TEST_LOG, VALIDATION_LOG))
