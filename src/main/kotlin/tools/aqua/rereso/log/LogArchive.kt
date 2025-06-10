// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalSerializationApi::class)

package tools.aqua.rereso.log

import kotlin.time.Duration
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import tools.aqua.rereso.log.Split.TRAINING
import tools.aqua.rereso.util.implies

/**
 * A ReReSo data format for string-based log archives. A log archive has an optional [name] and a
 * set of [logs].
 */
@Serializable data class LogArchive(val name: String? = null, val logs: Set<Log>)

/**
 * A single log in a [LogArchive].
 *
 * @property name a descriptive name, optional.
 * @property entries the entries in the log. If temporal information is available, these must be
 *   chronologically sorted and less than the [duration].
 * @property classifier a classifier for the log (e.g., `OutOfMemoryError`), optional.
 * @property split the training-validation-test split for pre-split logs, defaulting to [TRAINING].
 * @property epoch the absolute timestamp at which the log starts (i.e., a "recording start"),
 *   optional.
 * @property duration the temporal duration of the log (i.e., a "recording duration"), optional.
 */
@Serializable
data class Log(
    val name: String? = null,
    val entries: LogEntryList,
    @SerialName("class") val classifier: String? = null,
    val split: Split = TRAINING,
    val epoch: Instant? = null,
    val duration: Duration? = null,
) {
  /**
   * For logs with both an [epoch] and a [duration], the absolute timestamp of the log's end, else
   * `null`.
   */
  val end: Instant? = if (epoch != null && duration != null) epoch + duration else null

  init {
    entries.asSequence().mapNotNull(LogEntry::relativeStart).zipWithNext().forEach { (t1, t2) ->
      require(t1 <= t2) { "log must be chronological; violated by $t1 and $t2" }
    }
    if (duration != null) {
      entries.asSequence().mapNotNull(LogEntry::relativeStart).forEach {
        require(it <= duration) { "log start $it beyond log duration $duration" }
      }
      entries.asSequence().mapNotNull(LogEntry::relativeEnd).forEach {
        require(it <= duration) { "log end $it beyond log duration $duration" }
      }
    }
  }

  /** Helper constructor for string entries. */
  constructor(
      name: String? = null,
      entries: Iterable<String>,
      classifier: String? = null,
      split: Split = TRAINING,
      epoch: Instant? = null,
      duration: Duration? = null
  ) : this(name, entries.map(::LogEntry), classifier, split, epoch, duration)
}

private object LogEntryListSerializer : KSerializer<List<LogEntry>> {
  private val entryListSerializer = ListSerializer(LogEntry.generatedSerializer())

  override val descriptor: SerialDescriptor =
      buildClassSerialDescriptor("${LogEntry::class.qualifiedName}") {
        element("word", String.serializer().descriptor)
        element("entries", entryListSerializer.descriptor)
      }

  override fun deserialize(decoder: Decoder): List<LogEntry> =
      if (decoder !is JsonDecoder) {
        decoder.decodeSerializableValue(entryListSerializer)
      } else {
        val element = decoder.decodeJsonElement()
        when (element) {
          is JsonPrimitive -> element.content.toCharArray().map { LogEntry(it.toString()) }
          is JsonArray -> Json.decodeFromJsonElement(entryListSerializer, element)
          else -> error("unexpected type of $element")
        }
      }

  override fun serialize(encoder: Encoder, value: List<LogEntry>) =
      if (encoder !is JsonEncoder) {
        encoder.encodeSerializableValue(entryListSerializer, value)
      } else if (value.all { it.isSimple && it.value.length == 1 }) {
        encoder.encodeString(value.joinToString("", transform = LogEntry::value))
      } else {
        encoder.encodeSerializableValue(entryListSerializer, value)
      }
}

private typealias LogEntryList = @Serializable(with = LogEntryListSerializer::class) List<LogEntry>

private object LogEntrySerializer : KSerializer<LogEntry> {
  private val entrySerializer = LogEntry.generatedSerializer()

  override val descriptor: SerialDescriptor =
      buildClassSerialDescriptor("${LogEntry::class.qualifiedName}") {
        element("string", String.serializer().descriptor)
        element("entry", entrySerializer.descriptor)
      }

  override fun deserialize(decoder: Decoder): LogEntry =
      if (decoder !is JsonDecoder) {
        decoder.decodeSerializableValue(entrySerializer)
      } else {
        val element = decoder.decodeJsonElement()
        when (element) {
          is JsonPrimitive -> LogEntry(element.content)
          is JsonObject -> decoder.decodeSerializableValue(entrySerializer)
          else -> error("unexpected type of $element")
        }
      }

  override fun serialize(encoder: Encoder, value: LogEntry) =
      if (encoder !is JsonEncoder) {
        encoder.encodeSerializableValue(entrySerializer, value)
      } else if (value.isSimple) {
        encoder.encodeString(value.value)
      } else {
        encoder.encodeSerializableValue(entrySerializer, value)
      }
}

/**
 * An entry in a [Log].
 *
 * @property value the log entry.
 * @property denormalized if the entry was subject to normalization, the original entry, else
 *   `null`.
 * @property parameters if the entry was subject to normalization, the extracted parameters (if
 *   any), must be empty otherwise.
 * @property relativeStart the start of the entry relative to the containing [Log]'s `epoch`,
 *   optional.
 * @property duration the duration of the entry relative to the [start], optional.
 */
@Serializable(with = LogEntrySerializer::class)
@KeepGeneratedSerializer
data class LogEntry(
    val value: String,
    val denormalized: String? = null,
    val parameters: List<String> = emptyList(),
    @SerialName("relative-start") val relativeStart: Duration? = null,
    val duration: Duration? = null,
) {
  init {
    require((denormalized == null) implies parameters.isEmpty()) {
      "parameters $parameters are only valid if a denormalized string exists"
    }
    require(relativeStart?.isNegative() != true) {
      "relative start $relativeStart must be non-negative"
    }
    require(duration?.isNegative() != true) { "duration $duration must be non-negative" }
  }

  /**
   * If the entry has a [relativeStart] and a [duration], the end relative to the containing [Log]'s
   * `epoch`, else `null`.
   */
  val relativeEnd: Duration? =
      if (relativeStart != null && duration != null) relativeStart + duration else null

  /**
   * The absolute timestamp of the start, interpreted relative to [epoch]. `null` if the entry has
   * no [relativeStart].
   */
  fun start(epoch: Instant): Instant? = relativeStart?.let { epoch + it }

  /**
   * The absolute timestamp of the end, interpreted relative to [epoch]. `null` if the entry has no
   * [relativeEnd].
   */
  fun end(epoch: Instant): Instant? = relativeEnd?.let { epoch + it }

  /** True iff the entry only defines the [value] field. */
  val isSimple: Boolean =
      denormalized == null && parameters.isEmpty() && relativeStart == null && duration == null
}

/** The categories of a training-validation-test split. */
enum class Split {
  /** Data used in the creation of a machine learning model. */
  @SerialName("training") TRAINING,

  /** Data used to evaluate the performance of a machine learning model between rounds. */
  @SerialName("validation") VALIDATION,

  /** Data used to evaluate the final performance of a machine learned model. */
  @SerialName("test") TEST,
}
