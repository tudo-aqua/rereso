// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.testutil

import dev.harrel.jsonschema.Annotation
import dev.harrel.jsonschema.Error
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.assertj.core.api.Assertions.assertThat
import tools.aqua.rereso.Schema

private fun Error.toMessage(): String =
    "Error(evaluationPath=$evaluationPath, schemaLocation=$schemaLocation, instanceLocation=$instanceLocation, keyword=$keyword, error=$error)"

private fun Annotation.toMessage(): String =
    "Annotation(evaluationPath=$evaluationPath, schemaLocation=$schemaLocation, instanceLocation=$instanceLocation, keyword=$keyword, annotation=$annotation)"

internal inline fun <reified T> T.serializedFormConformsToSchemas(
    format: StringFormat,
    schemas: Iterable<Schema>
) {
  val encoded = format.encodeToString(this)
  schemas.forEach { schema ->
    val result = schema.validate(encoded)
    assertThat(result.isValid)
        .withFailMessage {
          "validation against $schema failed with errors: ${result.errors.map(Error::toMessage)}, annotations: ${result.annotations.map(Annotation::toMessage)}"
        }
        .isTrue
  }
}

internal inline fun <reified T> T.serializedFormConformsToSchemas(
    format: StringFormat,
    vararg schemas: Schema
) = serializedFormConformsToSchemas(format, schemas.asIterable())

internal inline fun <reified T> T.serializedFormViolatesSchemas(
    format: StringFormat,
    schemas: Iterable<Schema>
) {
  val encoded = format.encodeToString(this)
  schemas.forEach { schema ->
    val result = schema.validate(encoded)
    assertThat(result.isValid)
        .withFailMessage {
          "validation against $schema succeeded with errors: ${result.errors.map(Error::toMessage)}, annotations: ${result.annotations.map(Annotation::toMessage)}"
        }
        .isFalse
  }
}

internal inline fun <reified T> T.serializedFormViolatesSchemas(
    format: StringFormat,
    vararg schemas: Schema
) = serializedFormViolatesSchemas(format, schemas.asIterable())

internal inline fun <reified T> T.circularLoadingYieldsEqualObject(format: StringFormat) {
  val result = format.decodeFromString<T>(format.encodeToString(this))
  assertThat(result).isEqualTo(this)
}
