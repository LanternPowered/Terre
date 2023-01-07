/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.character

import org.lanternpowered.terre.item.Inventory
import org.lanternpowered.terre.item.ItemStack

interface CharacterStorage {

  /**
   * Loads all the inventory of the character.
   */
  suspend fun loadInventory(inventory: Inventory)

  /**
   * Saves the inventory of the character.
   */
  suspend fun saveInventory(inventory: Inventory)

  /**
   * Saves the inventory item at a specific [index].
   */
  suspend fun saveItem(index: Int, itemStack: ItemStack)
}
