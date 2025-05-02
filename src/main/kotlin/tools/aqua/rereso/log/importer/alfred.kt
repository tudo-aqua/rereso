// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.log.importer

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import tools.aqua.rereso.log.Log
import tools.aqua.rereso.log.LogArchive
import tools.aqua.rereso.log.LogEntry
import tools.aqua.rereso.log.Split.TRAINING
import tools.aqua.rereso.log.Split.VALIDATION
import tools.aqua.rereso.util.entriesIterator
import tools.aqua.rereso.util.ifNotEmpty
import tools.aqua.rereso.util.read

@Serializable
private data class AlfredTrajectoryData(
    @SerialName("pddl_params") val pddlParams: PDDLParams,
    val plan: AlfredPlan,
    @SerialName("task_id") val taskID: String,
    @SerialName("task_type") val taskType: String,
) {
  val classifier = "$taskType(${pddlParams.description})"
}

@Serializable
private data class PDDLParams(
    @SerialName("mrecep_target") val receptableTarget: String,
    @SerialName("object_sliced") val objectSliced: Boolean,
    @SerialName("object_target") val objectTarget: String,
    @SerialName("parent_target") val parentTarget: String,
    @SerialName("toggle_target") val toggleTarget: String,
) {
  val description: String =
      listOfNotNull(
              receptableTarget.ifNotEmpty("receptable="),
              if (objectSliced) "sliced" else null,
              objectTarget.ifNotEmpty("target="),
              parentTarget.ifNotEmpty("parent="),
              toggleTarget.ifNotEmpty("toggle="),
          )
          .joinToString()
}

@Serializable
private data class AlfredPlan(@SerialName("high_pddl") val highPDDL: List<PDDLAction>)

@Serializable
private data class PDDLAction(
    @SerialName("discrete_action") val discreteAction: DiscreteAction,
    @SerialName("high_idx") val highIndex: Int,
)

@Serializable private data class DiscreteAction(val action: String, val args: List<String>)

private val reader = Json { ignoreUnknownKeys = true }

/**
 * Import PDDL data from an ALFRED release into the ReReSo Log format.
 *
 * This is the `json.*.7z` linked in
 * [the ALFRED download script](https://github.com/askforalfred/alfred/blob/master/data/download_data.sh).
 * The `test` data is not imported, since it does not contain PDDL information.
 *
 * @param archive the downloaded data set. This is a 7Zip file containing JSON metadata.
 * @return the ReReSo [LogArchive] containing the data.
 */
fun importAlfred(archive: Path): LogArchive =
    SevenZFile.Builder().setPath(archive).get().use { sevenZip ->
      LogArchive(
          "ALFRED",
          buildSet {
            sevenZip.entriesIterator().forEach { entry ->
              if (entry.isDirectory) return@forEach

              val innerPath = Path(entry.name)
              if (innerPath.getName(1).name.startsWith("tests")) return@forEach

              this += importTrace(innerPath, sevenZip.read(entry).decodeToString())
            }
          })
    }

private fun importTrace(innerPath: Path, data: String): Log {
  val alfred = reader.decodeFromString<AlfredTrajectoryData>(data)

  return Log(
      alfred.taskID,
      alfred.plan.highPDDL.map { action ->
        LogEntry("${action.discreteAction.action}(${action.discreteAction.args.joinToString()})")
      },
      alfred.classifier,
      if (innerPath.getName(1).name == "train") TRAINING else VALIDATION)
}
