// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.log

import kotlinx.serialization.json.Json
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import tools.aqua.rereso.Schema
import tools.aqua.rereso.log.ReReSoLogSchemas.classifiedLogs
import tools.aqua.rereso.log.ReReSoLogSchemas.logs
import tools.aqua.rereso.log.ReReSoLogSchemas.normalizedLogs
import tools.aqua.rereso.log.ReReSoLogSchemas.timedLogs
import tools.aqua.rereso.log.ReReSoLogSchemas.tvtSplitLogs
import tools.aqua.rereso.testutil.circularLoadingYieldsEqualObject
import tools.aqua.rereso.testutil.serializedFormConformsToSchemas
import tools.aqua.rereso.testutil.serializedFormViolatesSchemas

internal class LogArchiveSchemaTest {

  internal data class LogClassificationTask(
      val logArchive: LogArchive,
      val satisfies: Set<Schema>,
      val violates: Set<Schema>
  )

  companion object {
    @JvmStatic
    val logArchives =
        listOf(
            LogClassificationTask(
                SIMPLE_LOG_ARCHIVE,
                setOf(logs, tvtSplitLogs),
                setOf(classifiedLogs, normalizedLogs, timedLogs)),
            LogClassificationTask(
                CHAR_LOG_ARCHIVE,
                setOf(logs, tvtSplitLogs),
                setOf(classifiedLogs, normalizedLogs, timedLogs)),
            LogClassificationTask(
                STRING_LOG_ARCHIVE,
                setOf(logs, tvtSplitLogs),
                setOf(classifiedLogs, normalizedLogs, timedLogs)),
            LogClassificationTask(
                CLASSIFIED_LOG_ARCHIVE,
                setOf(logs, classifiedLogs, tvtSplitLogs),
                setOf(normalizedLogs, timedLogs)),
            LogClassificationTask(
                PARTIALLY_CLASSIFIED_LOG_ARCHIVE,
                setOf(logs, tvtSplitLogs),
                setOf(classifiedLogs, normalizedLogs, timedLogs)),
            LogClassificationTask(
                NORMALIZED_LOG_ARCHIVE,
                setOf(logs, normalizedLogs, tvtSplitLogs),
                setOf(classifiedLogs, timedLogs)),
            LogClassificationTask(
                PARTIALLY_NORMALIZED_LOG_ARCHIVE,
                setOf(logs, tvtSplitLogs),
                setOf(classifiedLogs, normalizedLogs, timedLogs)),
            LogClassificationTask(
                TIMED_LOG_ARCHIVE,
                setOf(logs, timedLogs, tvtSplitLogs),
                setOf(classifiedLogs, normalizedLogs)),
            LogClassificationTask(
                PARTIALLY_TIMED_LOG_ARCHIVE,
                setOf(logs, tvtSplitLogs),
                setOf(classifiedLogs, normalizedLogs, timedLogs)),
            LogClassificationTask(
                TTV_SPLIT_LOG_ARCHIVE,
                setOf(logs, tvtSplitLogs),
                setOf(classifiedLogs, normalizedLogs, timedLogs)),
        )
  }

  @ParameterizedTest(name = "[{index}]")
  @FieldSource("logArchives")
  fun `log validates against certain schemas`(task: LogClassificationTask) {
    task.logArchive.serializedFormConformsToSchemas(Json, task.satisfies)
    task.logArchive.serializedFormViolatesSchemas(Json, task.violates)
  }

  @ParameterizedTest(name = "[{index}]")
  @FieldSource("logArchives")
  fun `log archive circular loading yields equal objects`(task: LogClassificationTask) =
      task.logArchive.circularLoadingYieldsEqualObject(Json)
}
