/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.characters

import org.lanternpowered.terre.character.CharacterStorage
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.character.InitCharacterStorageEvent
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.proxy.ProxyShutdownEvent
import org.lanternpowered.terre.item.Inventory
import org.lanternpowered.terre.item.ItemModifier
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.item.ItemTypeRegistry
import org.lanternpowered.terre.logger.Logger
import org.lanternpowered.terre.plugin.Plugin
import org.lanternpowered.terre.plugin.inject
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * A plugin that allows character storage to be configured.
 */
@Plugin(id = "characters")
object Characters {

  private val logger = inject<Logger>()
  private val storages = ConcurrentHashMap<UUID, CharacterStorage>()

  @Subscribe
  private fun onInit(event: ProxyInitializeEvent) {
    logger.info { "Initializing Characters plugin!" }
  }

  @Subscribe
  private fun onShutdown(event: ProxyShutdownEvent) {
  }

  @Subscribe
  private fun onInitCharacterStorage(event: InitCharacterStorageEvent) {
    // for testing an in memory storage
    val storage = storages.computeIfAbsent(event.player.uniqueId) {
      object : CharacterStorage {
        private val items = ConcurrentHashMap<Int, ItemStack>()

        init {
          items[0] = ItemStack(ItemTypeRegistry.require(3507), ItemModifier.Default)
          items[1] = ItemStack(ItemTypeRegistry.require(3506), ItemModifier.Default)
          items[2] = ItemStack(ItemTypeRegistry.require(3509), ItemModifier.Default)
        }

        override suspend fun loadInventory(inventory: Inventory) {
          items.forEach { (index, itemStack) ->
            inventory[index] = itemStack
          }
        }

        override suspend fun saveInventory(inventory: Inventory) {
          items.clear()
          inventory.forEach { (index, itemStack) ->
            items[index] = itemStack
          }
        }

        override suspend fun saveItem(index: Int, itemStack: ItemStack) {
          items[index] = itemStack
        }
      }
    }
    event.provide(storage)
  }
}
