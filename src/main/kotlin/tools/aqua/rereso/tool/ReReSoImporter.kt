// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.tool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.FileSystems
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div
import kotlin.io.path.name
import kotlin.io.path.notExists
import tools.aqua.rereso.log.LogArchive
import tools.aqua.rereso.log.importer.guessDiscExperiment
import tools.aqua.rereso.log.importer.guessDiscID
import tools.aqua.rereso.log.importer.importAbbadingoOne
import tools.aqua.rereso.log.importer.importAlfred
import tools.aqua.rereso.log.importer.importDiscTracePair
import tools.aqua.rereso.log.importer.importLogHubHadoopArchive
import tools.aqua.rereso.log.importer.importMitARMatlab
import tools.aqua.rereso.log.importer.importSplitNibeData
import tools.aqua.rereso.util.smartEncode

private class ReReSoImporter : CliktCommand(name = "rereso-import") {
  override fun run() = Unit
}

private abstract class SimpleImporter(name: String? = null) : CliktCommand(name) {
  protected val archive by
      argument().path(mustExist = true, canBeDir = false, mustBeReadable = true)
  protected val output by argument().path(canBeDir = false)

  final override fun run() {
    output.createParentDirectories()
    output.smartEncode(doImport())
  }

  protected abstract fun doImport(): LogArchive
}

private class AbbadingoOne : SimpleImporter(name = "abbadingo-one") {
  override fun doImport(): LogArchive = importAbbadingoOne(archive)
}

private class Alfred : SimpleImporter(name = "alfred") {
  override fun doImport(): LogArchive = importAlfred(archive)
}

private class DiscSingle : CliktCommand(name = "disc-single") {
  private val train by argument().path(mustExist = true, canBeDir = false, mustBeReadable = true)
  private val test by argument().path(mustExist = true, canBeDir = false, mustBeReadable = true)
  private val output by argument().path(canBeDir = false)
  private val experiment by
      option("-e", "--experiment").defaultLazy { guessDiscExperiment(train.parent) }
  private val id by option("-i", "--id").int().defaultLazy { guessDiscID(train) }

  override fun run() {
    output.createParentDirectories()
    output.smartEncode(importDiscTracePair(train, test, experiment, id))
  }
}

private class DiscAll : CliktCommand(name = "disc-all") {
  private companion object {
    val nameExpr = """train_([0-9])+\.txt""".toRegex()
  }

  private val dataDirectory by argument().path(mustExist = true, canBeFile = false)
  private val outputDirectory by argument().path(canBeFile = false)
  private val outputNameTemplate by
      option("-t", "--output-name-template").default("disc_<id>.json.zst").check {
        it.contains(FileSystems.getDefault().separator).not() && it.contains("<id>")
      }
  private val experiment by
      option("-e", "--experiment").defaultLazy { guessDiscExperiment(dataDirectory) }

  override fun run() {
    outputDirectory.createDirectories()
    for (train in dataDirectory) {
      val components = nameExpr.matchEntire(train.name) ?: continue
      val id = components.groupValues[1].toInt()
      val test = train.resolveSibling("test_$id.txt")
      if (test.notExists()) continue
      val output = outputDirectory / outputNameTemplate.replace("<id>", id.toString())
      output.smartEncode(importDiscTracePair(train, test, experiment, id))
    }
  }
}

private class HadoopLogHub : CliktCommand(name = "hadoop-loghub") {
  private val logHubArchive by
      argument().path(mustExist = true, canBeDir = false, mustBeReadable = true)
  private val logHub20Archive by
      argument().path(mustExist = true, canBeDir = false, mustBeReadable = true)
  private val outputDirectory by argument().path(canBeFile = false)
  private val outputNameTemplate by
      option("-t", "--output-name-template").default("<id>.json.zst").check {
        it.contains(FileSystems.getDefault().separator).not() && it.contains("<id>")
      }

  override fun run() {
    outputDirectory.createDirectories()
    importLogHubHadoopArchive(logHubArchive, logHub20Archive).forEach { (application, logs) ->
      val output = outputDirectory / outputNameTemplate.replace("<id>", application)
      output.smartEncode(logs)
    }
  }
}

private class MitAR : SimpleImporter(name = "mit-ar") {
  val strictActivities by option("-s", "--strict-activities").flag("-r", "--relaxed-activities")

  override fun doImport(): LogArchive = importMitARMatlab(archive, strictActivities)
}

private class NiBe : SimpleImporter(name = "nibe") {
  override fun doImport(): LogArchive = importSplitNibeData(archive)
}

internal fun main(args: Array<String>) =
    ReReSoImporter()
        .subcommands(
            AbbadingoOne(), Alfred(), DiscSingle(), DiscAll(), HadoopLogHub(), MitAR(), NiBe())
        .main(args)
