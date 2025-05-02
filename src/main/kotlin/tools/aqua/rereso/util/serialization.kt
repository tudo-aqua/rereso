// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalSerializationApi::class)

package tools.aqua.rereso.util

import jakarta.activation.MimeType
import java.net.URI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object MimeTypeSerializer : KSerializer<MimeType> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("${javaClass.packageName}.MimeType", STRING)

  override fun serialize(encoder: Encoder, value: MimeType) = encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): MimeType = MimeType(decoder.decodeString())
}

internal typealias SerializableMimeType = @Serializable(with = MimeTypeSerializer::class) MimeType

internal class NullifyEmptyListSerializer<T>(dataSerializer: KSerializer<T>) :
    KSerializer<List<T>> {
  private val delegateSerializer = ListSerializer(dataSerializer).nullable
  override val descriptor: SerialDescriptor =
      SerialDescriptor("$${javaClass.packageName}.NullifyEmptyList", delegateSerializer.descriptor)

  override fun serialize(encoder: Encoder, value: List<T>) =
      encoder.encodeSerializableValue(delegateSerializer, if (value.isEmpty()) null else value)

  override fun deserialize(decoder: Decoder): List<T> =
      decoder.decodeSerializableValue(delegateSerializer) ?: emptyList()
}

internal typealias NullifiedEmptyList<T> =
    @Serializable(with = NullifyEmptyListSerializer::class) List<T>

internal class NullifyEmptySetSerializer<T>(dataSerializer: KSerializer<T>) : KSerializer<Set<T>> {
  private val delegateSerializer = SetSerializer(dataSerializer).nullable
  override val descriptor: SerialDescriptor =
      SerialDescriptor("$${javaClass.packageName}.NullifyEmptySet", delegateSerializer.descriptor)

  override fun serialize(encoder: Encoder, value: Set<T>) =
      encoder.encodeSerializableValue(delegateSerializer, if (value.isEmpty()) null else value)

  override fun deserialize(decoder: Decoder): Set<T> =
      decoder.decodeSerializableValue(delegateSerializer) ?: emptySet()
}

internal typealias NullifiedEmptySet<T> =
    @Serializable(with = NullifyEmptySetSerializer::class) Set<T>

internal object URISerializer : KSerializer<URI> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("${javaClass.packageName}.URI", STRING)

  override fun serialize(encoder: Encoder, value: URI) = encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): URI = URI(decoder.decodeString())
}

internal typealias SerializableURI = @Serializable(with = URISerializer::class) URI
