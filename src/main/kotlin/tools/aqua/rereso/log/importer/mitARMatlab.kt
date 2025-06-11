// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.log.importer

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.apache.commons.compress.archivers.zip.ZipFile
import tools.aqua.rereso.log.Log
import tools.aqua.rereso.log.LogArchive
import tools.aqua.rereso.log.LogEntry
import tools.aqua.rereso.util.contains
import tools.aqua.rereso.util.overlaps
import us.hebi.matlab.mat.format.Mat5.newReader as mat5Reader
import us.hebi.matlab.mat.format.Mat5File
import us.hebi.matlab.mat.types.Cell
import us.hebi.matlab.mat.types.Matrix
import us.hebi.matlab.mat.types.Sources.wrapInputStream

private val subject = "subject([0-9]+)".toRegex()

private val bostonTZ = TimeZone.of("America/New_York")

/**
 * Import a MIT Activity Recognition data set in Matlab 5 format into the ReReSo Log format.
 *
 * This is the `thesis_data_matlab.zip` available at
 * [the MIT course page](https://courses.media.mit.edu/2004fall/mas622j/04.projects/home/).
 *
 * @param archive the downloaded archive file to read. This is a ZIP archive containing five `mat`
 *   files per subject.
 * @param strictActivities true iff overlapping activities are to be discarded.
 * @return the ReReSo [LogArchive] containing the (cleaned) data.
 */
fun importMitARMatlab(archive: Path, strictActivities: Boolean): LogArchive =
    ZipFile.Builder().setPath(archive).get().use { zip ->
      LogArchive(
          "Activity Recognition in the Home Setting Using Simple and Ubiquitous Sensors",
          zip.entries
              .asSequence()
              .filter { it.isDirectory }
              .map { it.name to Path(it.name) }
              .filter { (_, path) -> path.count() == 2 }
              .mapNotNull { (name, path) ->
                subject.matchEntire(path.getName(1).name)?.let { name to it.groupValues[1].toInt() }
              }
              .flatMap { (path, subject) ->
                importSubjectData(zip, path, subject, strictActivities)
              }
              .toSet())
    }

private fun importSubjectData(
    zip: ZipFile,
    basePath: String,
    subject: Int,
    strictActivities: Boolean
): List<Log> {
  fun <T> useMat(name: String, block: (Mat5File) -> T): T =
      wrapInputStream(zip.getInputStream(zip.getEntry("$basePath$name$subject.mat"))).use {
        block(mat5Reader(it).readMat())
      }

  fun getMat(name: String) =
      mat5Reader(wrapInputStream(zip.getInputStream(zip.getEntry("$basePath$name$subject.mat"))))
          .readMat()

  val locations = useMat("locations") { mat -> mat.getCell(0).use { it.toStringList() } }
  val appliances = useMat("types") { mat -> mat.getCell(0).use { it.toStringList() } }
  val activities = useMat("classes") { mat -> mat.getCell(0).use { it.toActivityClasses() } }
  val activityData =
      useMat("activities_data") { mat -> mat.getMatrix(0).use { it.toActivityData(activities) } }
  val sensorData =
      useMat("sensor_data") { mat ->
        mat.getMatrix(0).use { it.toSensorData(locations, appliances) }
      }

  val cleanActivityData = if (strictActivities) cleanupActivityData(activityData) else activityData

  return mergeData(cleanActivityData, sensorData).toList().withIndex().mapNotNull { (index, data) ->
    val (activity, entries) = data
    if (entries.isEmpty()) return@mapNotNull null
    val activityStart = activity.start.toInstant(bostonTZ)
    Log(
        "subject $subject #$index",
        entries.map {
          val entryStart = it.start.toInstant(bostonTZ)
          LogEntry(
              it.appliance,
              relativeStart = entryStart - activityStart,
              duration = it.end.toInstant(bostonTZ) - entryStart)
        },
        activity.activity.activity,
        epoch = activity.start.toInstant(bostonTZ),
        duration = activity.end.toInstant(bostonTZ) - activityStart)
  }
}

private fun Cell.getCharAsString(row: Int, column: Int): String =
    getChar(row, column).use { it.string }

private fun Cell.toStringList(): List<String> {
  require(numDimensions == 2)
  val (rows, cols) = dimensions
  require(cols == 1)
  return (0..<rows).map { row -> getCharAsString(row, 0) }
}

private data class Activity(val category: String, val group: String, val activity: String)

private fun Cell.toActivityClasses(): Map<Int, Activity> {
  require(numDimensions == 2)
  val (rows, cols) = dimensions
  require(cols == 4)
  return (0..<rows).associate { row ->
    // do not autoclose the matrices used here, this must be done by the surrounding cell
    getMatrix(row, 3).getInt(0) to
        Activity(getCharAsString(row, 0), getCharAsString(row, 1), getCharAsString(row, 2))
  }
}

private data class ActivityData(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val activity: Activity,
    val importantForHealthApplications: Boolean,
) {
  val timespan: ClosedRange<LocalDateTime> = start..end
}

private fun Matrix.toActivityData(activities: Map<Int, Activity>): List<ActivityData> {
  require(numDimensions == 2)
  val (rows, cols) = dimensions
  require(cols == 7)
  return (0..<rows).map { row ->
    val date = LocalDate(2003, 1, 1) + DatePeriod(days = getInt(row, 0))
    check(DayOfWeek(getInt(row, 1)) == date.dayOfWeek)
    val start = LocalTime.fromSecondOfDay(getInt(row, 2))
    val activity = activities.getValue(getInt(row, 3))
    val end = LocalTime.fromSecondOfDay(getInt(row, 5))
    val indicator = getInt(row, 6)

    ActivityData(LocalDateTime(date, start), LocalDateTime(date, end), activity, indicator != 0)
  }
}

private data class SensorData(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val sensorID: Int,
    val location: String,
    val appliance: String,
) {
  val timespan: ClosedRange<LocalDateTime> = start..end
}

private fun Matrix.toSensorData(
    locations: List<String>,
    appliances: List<String>
): List<SensorData> {
  require(numDimensions == 2)
  val (rows, cols) = dimensions
  require(cols == 8)
  return (0..<rows).map { row ->
    val date = LocalDate(2003, 1, 1) + DatePeriod(days = getInt(row, 0))
    check(DayOfWeek(getInt(row, 1)) == date.dayOfWeek)
    val start = LocalTime.fromSecondOfDay(getInt(row, 2))
    val end = LocalTime.fromSecondOfDay(getInt(row, 3))
    check(getInt(row, 4) == end.toSecondOfDay() - start.toSecondOfDay())
    val sensorID = getInt(row, 5)
    val location = locations[getInt(row, 6) - 1]
    val appliance = appliances[getInt(row, 7) - 1]

    SensorData(LocalDateTime(date, start), LocalDateTime(date, end), sensorID, location, appliance)
  }
}

private fun cleanupActivityData(activityData: List<ActivityData>): List<ActivityData> = buildList {
  activityData.forEach { ad ->
    check(none { ad.timespan overlaps it.timespan })
    removeIf { it.timespan contains ad.timespan }
    this += ad
  }
}

private fun mergeData(
    activityData: List<ActivityData>,
    sensorData: List<SensorData>
): Map<ActivityData, List<SensorData>> {
  val activitiesIter = activityData.sortedBy { it.start }.iterator()
  val sensedIter = sensorData.sortedBy { it.start }.listIterator()

  return buildMap<ActivityData, MutableList<SensorData>> {
    activitiesIter.forEach { activity ->
      val sensedList = mutableListOf<SensorData>()
      this[activity] = sensedList
      while (sensedIter.hasNext()) {
        val sensed = sensedIter.next()
        when {
          sensed.start >= activity.start && sensed.start < activity.end -> sensedList += sensed
          sensed.start > activity.end -> {
            sensedIter.previous() // we have gone too far, back off
            break
          }
        }
      }
    }
  }
}
