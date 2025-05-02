// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.log.importer

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.InputStream
import java.nio.file.Path
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone.Companion.UTC
import kotlinx.datetime.toInstant
import tools.aqua.rereso.log.Log
import tools.aqua.rereso.log.LogArchive
import tools.aqua.rereso.log.LogEntry
import tools.aqua.rereso.util.asZip
import tools.aqua.rereso.util.getInputStream

/**
 * Import the LogHub Hadoop archive and normalize it using the LogHub-2.0 normalization data.
 *
 * This requires the Hadoop ZIP from the [LogHub data set](https://doi.org/10.5281/zenodo.8196385)
 * and the Hadoop ZIP from the [LogHub-2.0 data set](https://doi.org/10.5281/zenodo.8275860). Some
 * data cleaning is applied to the files.
 *
 * @param logHubArchive the LogHub data set. This is a ZIP file containing multiple `.log` files.
 * @param logHub20Archive the LogHub-2.0 data set. This is a ZIP file containing
 *   `Hadoop_full.log_templates.csv`.
 * @return the ReReSo [LogArchive] containing the data.
 */
fun importLogHubHadoopArchive(
    logHubArchive: Path,
    logHub20Archive: Path,
): Map<String, LogArchive> {
  val templates =
      logHub20Archive.asZip().use { zip ->
        zip.getInputStream("Hadoop/Hadoop_full.log_templates.csv").use { parseLogTemplates(it) }
      }

  logHubArchive.asZip().use { zip ->
    val labels = zip.getInputStream("abnormal_label.txt").use(::parseAbnormalLabels)
    return zip.entries
        .toList()
        .filter { it.name.contains('/') && !it.name.endsWith('/') }
        .map {
          val (dir, _) = it.name.split('/', limit = 2)
          val (application, label) = labels.getValue(dir)
          Triple(application, label, it)
        }
        .groupBy({ it.first }, { it.second to it.third })
        .mapValues { (application, logs) ->
          LogArchive(
              "LogHub Hadoop $application with LogHub-2.0 Templates",
              logs.mapTo(mutableSetOf()) { (label, entry) ->
                zip.getInputStream(entry).use { parseLogFile(it, templates, entry.name, label) }
              })
        }
  }
}

private data class LogTemplate(val regex: Regex, val template: String)

private fun parseLogTemplates(source: InputStream): List<LogTemplate> =
    csvReader()
        .readAllWithHeader(source)
        .map { line ->
          val template = line.getValue("EventTemplate")
          val litaralParts = template.split("<*>").map { Regex.escape(it) }
          val stars = litaralParts.size - 1
          val regex = litaralParts.joinToString("(.*)").toRegex()
          stars to (LogTemplate(regex, template))
        }
        .sortedByDescending { it.first }
        .map { it.second }

private fun parseAbnormalLabels(source: InputStream): Map<String, Pair<String, String>> = buildMap {
  var application: String? = null
  var label: String? = null
  source.bufferedReader().forEachLine {
    when {
      it.startsWith("### ") -> application = it.removePrefix("### ")
      it.endsWith(":") -> label = it.removeSuffix(":")
      it.startsWith("+ ") -> this[it.removePrefix("+ ")] = application!! to label!!
    }
  }
}

private val lineRegex =
    """([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2}),([0-9]{3}) ([A-Z]+) \[([^]]+)] ([^ :]+): (.+)"""
        .toRegex()
private val spaces = "  +".toRegex()

private fun parseLogFile(
    source: InputStream,
    logTemplates: List<LogTemplate>,
    name: String,
    classifier: String
): Log {
  var firstStart: Instant? = null

  val entries = buildList {
    source.bufferedReader().forEachLine { line ->
      val match = lineRegex.matchEntire(line) ?: return@forEachLine
      val (year, month, day) = match.groupValues.subList(1, 4).map(String::toInt)
      val (hour, minute, second, millisecond) = match.groupValues.subList(4, 8).map(String::toInt)
      val timestamp = LocalDateTime(year, month, day, hour, minute, second, millisecond * 1_000_000)

      val message = match.groupValues[11].trim().replace(spaces, " ")
      val messageTemplate =
          logTemplates
              .asSequence()
              .map { (regex, template) -> regex.matchEntire(message) to template }
              .firstOrNull { it.first != null } ?: return@forEachLine
      val parameters = messageTemplate.first!!.groupValues.let { it.subList(1, it.size) }

      val start = timestamp.toInstant(UTC)
      if (firstStart == null) firstStart = start

      this +=
          LogEntry(
              messageTemplate.second,
              denormalized = line,
              parameters = parameters,
              relativeStart = timestamp.toInstant(UTC) - firstStart)
    }
  }

  return Log(name, entries, classifier = classifier, epoch = firstStart)
}
