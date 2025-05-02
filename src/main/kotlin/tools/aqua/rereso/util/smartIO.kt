// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalSerializationApi::class)

package tools.aqua.rereso.util

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.outputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

/**
 * Open an [OutputStream] to this file, transparently compressing based on file name extension.
 *
 * This returns the opened stream and the “virtual” file path of the decompressed file, i.e., the
 * file path with stripped compresion extension.
 */
fun Path.compressingOutputStreamAndName(): Pair<OutputStream, Path> =
    when (extension.lowercase()) {
      "bz2" -> outputStream().buffered().bzip2() to resolveSibling(nameWithoutExtension)
      "gz" -> outputStream().buffered().gzip() to resolveSibling(nameWithoutExtension)
      "xz" -> outputStream().buffered().xz() to resolveSibling(nameWithoutExtension)
      "zst" -> outputStream().buffered().zstd() to resolveSibling(nameWithoutExtension)
      "zstd" -> outputStream().buffered().zstd() to resolveSibling(nameWithoutExtension)
      else -> outputStream() to this
    }

/**
 * Open an [InputStream] to this file, transparently decompressing based on file name extension.
 *
 * This returns the opened stream and the “virtual” file path of the decompressed file, i.e., the
 * file path with stripped compresion extension.
 */
fun Path.decompressingInputStreamAndName(): Pair<InputStream, Path> =
    when (extension.lowercase()) {
      "bz2" -> inputStream().buffered().bunzip2() to resolveSibling(nameWithoutExtension)
      "gz" -> inputStream().buffered().gunzip() to resolveSibling(nameWithoutExtension)
      "xz" -> inputStream().buffered().unxz() to resolveSibling(nameWithoutExtension)
      "zst" -> inputStream().buffered().unzstd() to resolveSibling(nameWithoutExtension)
      "zstd" -> inputStream().buffered().unzstd() to resolveSibling(nameWithoutExtension)
      else -> inputStream() to this
    }

/** Open an [OutputStream] to this file, transparently compressing based on file name extension. */
fun Path.compressingOutputStream(): OutputStream = compressingOutputStreamAndName().first

/** Open an [InputStream] to this file, transparently decompressing based on file name extension. */
fun Path.decompressingInputStream(): InputStream = decompressingInputStreamAndName().first

/**
 * Use Kotlin Serialization to load an object of type [T] from this file, transparently
 * decompressing based on file name extension and the selecting the serialization format based on
 * (remaining) file name extension.
 */
inline fun <reified T> Path.smartDecode(): T {
  val (decompressed, pseudoPath) = decompressingInputStreamAndName()
  return decompressed.buffered().use { input ->
    when (pseudoPath.extension.lowercase()) {
      "json" -> Json.decodeFromStream<T>(input)
      "json5" -> json5.decodeFromStream<T>(input)
      "yaml" -> Yaml.default.decodeFromStream<T>(input)
      "yml" -> Yaml.default.decodeFromStream<T>(input)
      else -> error { "unsupported serialization format: ${pseudoPath.extension.lowercase()}" }
    }
  }
}

/**
 * Use Kotlin Serialization to store [value] of type [T] to this file, transparently compressing
 * based on file name extension and the selecting the serialization format based on (remaining) file
 * name extension.
 */
inline fun <reified T> Path.smartEncode(value: T) {
  val (compressed, pseudoPath) = compressingOutputStreamAndName()
  return compressed.buffered().use { output ->
    when (pseudoPath.extension.lowercase()) {
      "json" -> Json.encodeToStream(value, output)
      "json5" -> json5.encodeToStream(value, output)
      "yaml" -> Yaml.default.encodeToStream(value, output)
      "yml" -> Yaml.default.encodeToStream(value, output)
      else ->
          throw IllegalArgumentException(
              "unsupported serialization format: ${pseudoPath.extension.lowercase()}")
    }
  }
}
