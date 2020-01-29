/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.plugin

import org.lanternpowered.terre.util.`use named arguments`

/**
 * An annotation to mark plugin classes or objects.
 *
 * @property id The id, can only contain a-z, 0-9, - and _
 * @property name The name, this is more human readable than the id
 * @property version The version
 * @property description The description
 * @property url The url to the website of the plugin
 * @property authors The authors
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Plugin(
    vararg val `use named arguments`: `use named arguments`,
    val id: String,
    val name: String = "",
    val version: String = "",
    val description: String = "",
    val url: String = "",
    val authors: Array<String> = []
)
