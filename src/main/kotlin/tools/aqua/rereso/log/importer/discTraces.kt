// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.log.importer

import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.useLines
import tools.aqua.rereso.log.Log
import tools.aqua.rereso.log.LogArchive
import tools.aqua.rereso.log.LogEntry
import tools.aqua.rereso.log.Split
import tools.aqua.rereso.log.Split.TEST
import tools.aqua.rereso.log.Split.TRAINING

/**
 * Import a training/test pair of DISC traces from "Interpretable Sequence Classification via
 * Discrete Optimization".
 *
 * This requires files from the [authors' GitHub](https://github.com/andrewli77/DISC) at
 * `traces/experiment/train_i.txt` and `traces/experiment/test_i.txt`. File name and path are used
 * to derive metadata.
 *
 * @param train the training file. This is a text file.
 * @param test the corresponding test file. This is a text file.
 * @param experiment the name of the experiment. Can be automatically deducted if the files are in
 *   their default directory structure.
 * @param id the set ID. Can be automatically deducted if the files were not renamed.
 * @return the ReReSo [LogArchive] containing the data.
 */
fun importDiscTracePair(
    train: Path,
    test: Path,
    experiment: String = guessDiscExperiment(train.parent),
    id: Int = guessDiscID(train)
): LogArchive =
    LogArchive(
        "DISC Reproduction Package $experiment #$id",
        importDISCTraces(train, TRAINING) + importDISCTraces(test, TEST))

private val traceRegex = "(?:test|train)_([0-9]+).txt".toRegex()

internal fun guessDiscID(path: Path): Int =
    traceRegex.matchEntire(path.name)?.groupValues?.getOrNull(1)?.toInt()
        ?: error("could not parse id from file name ${path.name}")

internal fun guessDiscExperiment(path: Path): String {
  var maybeRoot = path.parent
  while (maybeRoot != null) {
    if (maybeRoot.name == "traces") {
      return maybeRoot.relativize(path).joinToString("/") { it.name }
    }
    maybeRoot = maybeRoot.parent
  }
  error("did not find the trace root in $path")
}

private fun importDISCTraces(path: Path, split: Split = guessDiscSplit(path)): Set<Log> =
    path.useLines { lines ->
      lines
          .withIndex()
          .map { (index, line) ->
            val (trace, classifier) = line.split(';')
            Log(
                "${split.name.lowercase()} #$index",
                trace.split(",").map(::LogEntry),
                classifier,
                split)
          }
          .toSet()
    }

private fun guessDiscSplit(path: Path): Split =
    when {
      path.name.startsWith("test") -> TEST
      path.name.startsWith("train") -> TRAINING
      else -> error("could not parse split from file name ${path.name}")
    }
