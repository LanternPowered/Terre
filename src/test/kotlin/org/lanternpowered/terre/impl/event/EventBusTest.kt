/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.event

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.event.Listener
import org.lanternpowered.terre.plugin.PluginContainer

class EventBusTest {

  private val plugin = SimplePluginContainer("test")

  @Test fun `test generic registration`(): Unit = runBlocking {
    val eventBus = TerreEventBus

    eventBus.register<TestEvent>(plugin) { event ->
      println(event.value)
    }

    GlobalScope.launch {
      eventBus.post(TestEvent(100))
    }
  }

  @Test fun `test instance registration`(): Unit = runBlocking {
    val eventBus = TerreEventBus
    eventBus.register(plugin, TestListeners())

    GlobalScope.launch {
      eventBus.post(TestEvent(100))
    }
  }

  class TestListeners {

    @Listener fun onTest(event: TestEvent) {
      println(event.value)
    }

    @Listener suspend fun onSuspendTest(event: TestEvent) {
      println(event.value)
    }
  }

  class TestEvent(val value: Int) : Event

  class SimplePluginContainer(
      override val id: String,
      override val name: String = id,
      override val description: String? = null,
      override val authors: List<String> = emptyList(),
      override val instance: Any = Any()
  ) : PluginContainer
}
