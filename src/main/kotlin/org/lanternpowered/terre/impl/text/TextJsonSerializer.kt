/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.text

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf

internal class TextJsonDeserializer : StdDeserializer<Text>(Text::class.java) {

  override fun deserialize(parser: JsonParser, context: DeserializationContext): Text {
    // val literal = parser.readValueAs(String::class.java)
    val node = parser.codec.readTree<JsonNode>(parser)
    val literal = node["text"].asText()
    // TODO

    return textOf(literal)
  }
}

internal class TextJsonSerializer : StdSerializer<Text>(Text::class.java) {

  override fun serialize(value: Text, gen: JsonGenerator, provider: SerializerProvider) {
    // gen.writeString(value.toPlain())
    gen.writeStartObject()
    gen.writeStringField("text", value.toPlain())
    gen.writeEndObject()
  }
}
