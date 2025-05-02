// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalSerializationApi::class)

package tools.aqua.rereso

import java.util.Objects.hash
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import tools.aqua.rereso.util.NullifiedEmptySet
import tools.aqua.rereso.util.NullifyEmptyListSerializer
import tools.aqua.rereso.util.SerializableMimeType
import tools.aqua.rereso.util.SerializableURI

/**
 * A data format. This indicates a) the syntax of the file as a [mediaType] and b) the semantics of
 * the contents as [schemas] satisfied by the data.
 */
@Serializable
data class DataFormat(
    @SerialName("media-type") val mediaType: SerializableMimeType,
    @EncodeDefault(NEVER) val schemas: NullifiedEmptySet<SerializableURI> = emptySet(),
) {
  init {
    require(mediaType.parameters.isEmpty) { "the media type must not be parameterized" }
  }

  override fun equals(other: Any?): Boolean =
      when {
        this === other -> true
        other !is DataFormat -> false
        else -> mediaType.baseType == other.mediaType.baseType && schemas == other.schemas
      }

  override fun hashCode(): Int = hash(mediaType.baseType, schemas)
}

private object LicenseAsDataSerializer : KSerializer<License> {
  private val delegateSerializer = LicenseData.serializer()
  override val descriptor: SerialDescriptor =
      SerialDescriptor("${javaClass.packageName}.LicenseAsData", delegateSerializer.descriptor)

  override fun serialize(encoder: Encoder, value: License) =
      encoder.encodeSerializableValue(delegateSerializer, LicenseData(value))

  override fun deserialize(decoder: Decoder): License =
      decoder.decodeSerializableValue(delegateSerializer).license
}

@Serializable
private data class LicenseData(
    @EncodeDefault(NEVER) private val spdx: SpdxLicense? = null,
    @EncodeDefault(NEVER) private val custom: CustomLicense? = null
) {
  constructor(license: License) : this(license as? SpdxLicense, license as? CustomLicense)

  init {
    require((spdx != null) xor (custom != null)) { "only one type of license may be used" }
  }

  val license: License
    get() = spdx ?: custom!!
}

/** A license; either a [SpdxLicense] or a [CustomLicense]. */
@Serializable(with = LicenseAsDataSerializer::class) sealed interface License

/** A standard license string [id] as defined by the SPDX specification. */
@Serializable @JvmInline value class SpdxLicense(val id: String) : License

/** Information about a non-SPDX license arrangement as [text]. */
@Serializable @JvmInline value class CustomLicense(val text: String) : License

/**
 * Generic metadata for a ReReSo artifact. This includes a [name], an optional [description], and a
 * list of [references] relevant to the data.
 */
@Serializable
data class Metadata(
    val name: String,
    @EncodeDefault(NEVER) val description: String? = null,
    @EncodeDefault(NEVER)
    @Serializable(with = NullifyEmptyListSerializer::class)
    private val references: List<SerializableURI> = emptyList()
)
