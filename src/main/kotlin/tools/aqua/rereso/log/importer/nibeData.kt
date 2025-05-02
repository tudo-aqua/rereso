// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.log.importer

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.InputStream
import java.nio.file.Path
import kotlinx.datetime.Instant
import tools.aqua.rereso.log.Log
import tools.aqua.rereso.log.LogArchive
import tools.aqua.rereso.log.LogEntry
import tools.aqua.rereso.util.asZip
import tools.aqua.rereso.util.mapToSet

/**
 * Import a Nibe data set in CSV format into the ReReSo Log format.
 *
 * These files are not publicly available at the moment
 *
 * @param archive the downloaded archive file to read. This is a ZIP archive containing a CSV file
 *   per event type and split series.
 * @return the ReReSo [LogArchive] containing the data.
 */
fun importSplitNibeData(archive: Path): LogArchive =
    archive.asZip().use { zip ->
      LogArchive(
          "Nibe Data",
          zip.entries
              .toList()
              .filter { it.name.endsWith(".csv") }
              .mapToSet { readTrace(zip.getInputStream(it), it.name.removeSuffix(".csv")) })
    }

private fun readTrace(input: InputStream, name: String): Log =
    input.use {
      val lines =
          csvReader().readAllWithHeader(it).map { line ->
            val timestamp = Instant.fromEpochSeconds(line.getValue("timestamp").toLong())
            val event = line.getValue("event")
            timestamp to event
          }
      val firstTimestamp = lines.minOf { (timestamp, _) -> timestamp }
      Log(
          name,
          lines.map { (timestamp, event) ->
            LogEntry(event, relativeStart = timestamp - firstTimestamp)
          },
          epoch = firstTimestamp)
    }
