// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.log.importer

import java.nio.file.Path
import kotlin.io.path.name
import tools.aqua.rereso.log.Log
import tools.aqua.rereso.log.LogArchive
import tools.aqua.rereso.log.LogEntry
import tools.aqua.rereso.log.Split.TEST
import tools.aqua.rereso.log.Split.TRAINING
import tools.aqua.rereso.util.decompressingInputStream

private val idRegex = """\(.*\)\.([0-9a-z])\.gz""".toRegex()

/**
 * Import an Abbaddingo One training data set into the ReReSo Log format.
 *
 * These are the `train.*.gz` and `test.*.gz` files available at
 * [the Abbadingo data sets page](https://abbadingo.cs.nuim.ie/data-sets.html). Note that the test
 * data does not contain classifiers.
 *
 * @param archive the downloaded training set to read. This is a GZIP file named `something.id.gz`,
 *   with `id` being the problem set ID.
 * @return the ReReSo [LogArchive] containing the data.
 */
fun importAbbadingoOne(archive: Path): LogArchive =
    LogArchive(
        name =
            idRegex.matchEntire(archive.name)?.let {
              "Abbadingo Problem ${it.groupValues[2]} ${it.groupValues[1]} data"
            } ?: "Abbadingo Problem",
        logs =
            archive.decompressingInputStream().bufferedReader().use { reader ->
              val (nLines, nClasses) = reader.readLine().split(' ').map { it.toInt() }

              val lines = reader.lines().toList()
              require(nLines == lines.size)

              lines.withIndex().mapTo(mutableSetOf()) { (index, lineData) ->
                val line = lineData.split(' ')

                val (classifier, length) = line.subList(0, 2).map { it.toInt() }
                require(length.toInt() + 2 == line.size) {
                  "incorrect length of line at $index: $line"
                }

                val (realClassifier, split) =
                    if (classifier !in 0..<nClasses) null to TEST
                    else classifier.toString() to TRAINING
                Log(
                    "line #$index",
                    line.subList(2, line.size).map(::LogEntry),
                    realClassifier,
                    split)
              }
            })
