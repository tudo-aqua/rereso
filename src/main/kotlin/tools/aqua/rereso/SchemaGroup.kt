// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso

import dev.harrel.jsonschema.Validator
import dev.harrel.jsonschema.Validator.Result
import dev.harrel.jsonschema.ValidatorFactory
import dev.harrel.jsonschema.providers.KotlinxJsonNode
import dev.harrel.jsonschema.providers.SnakeYamlNode
import java.io.BufferedReader
import java.net.URI
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import tools.aqua.rereso.util.camelToSnakeCase
import tools.aqua.rereso.util.json5

/**
 * A JSON schema that can be used to validate documents.
 *
 * @param validator the validator object to use for validation
 * @param uri the URI identifying this schema.
 */
class Schema internal constructor(private val validator: Validator, private val uri: URI) {
  /** Attempt to validate [document] against this schema. */
  fun validate(document: String): Result = validator.validate(uri, document)

  override fun toString(): String = uri.toString()
}

/** A set of interrelated schemas. The share a [validator] */
abstract class SchemaGroup(private val validator: Validator) {

  /** The schema version implemented by this library. */
  abstract val version: String

  /**
   * Add a new schema to this group.
   *
   * The schema file is expected to be located in the implementing class' package and named
   * `<dash-case>.json5`, where `<dash-case>` is [name] converted from camel case to snake case
   * using `-` as a snake characted (i.e., `theThing` becomes `the-thing`).
   */
  protected fun registerBundledSchema(name: String): Schema {
    val name =
        "/${javaClass.packageName.replace('.', '/')}/${name.camelToSnakeCase("-")}-$version.json5"
    val content =
        javaClass.getResourceAsStream(name)?.bufferedReader()?.use(BufferedReader::readText)
    checkNotNull(content) { "Schema $name not found" }
    val uri = validator.registerSchema(content)
    return Schema(validator, uri)
  }

  /** Delegate version of [registerBundledSchema]. The field's name is used as the schema name. */
  protected val registeringBundledSchema:
      PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Schema>>
    get() = PropertyDelegateProvider { _, property ->
      val schema = registerBundledSchema(property.name)
      ReadOnlyProperty { _, _ -> schema }
    }
}

internal fun jsonValidator() =
    ValidatorFactory()
        .withJsonNodeFactories(KotlinxJsonNode.Factory(json5), KotlinxJsonNode.Factory())
        .createValidator()

internal fun json5Validator() =
    ValidatorFactory()
        .withJsonNodeFactories(KotlinxJsonNode.Factory(json5), KotlinxJsonNode.Factory(json5))
        .createValidator()

internal fun yamlValidator() =
    ValidatorFactory()
        .withJsonNodeFactories(KotlinxJsonNode.Factory(json5), SnakeYamlNode.Factory())
        .createValidator()
