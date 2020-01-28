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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.lanternpowered.terre.dispatcher.runAsync
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.event.EventBus
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.subscribe
import org.lanternpowered.terre.plugin.PluginContainer
import java.util.concurrent.atomic.LongAdder
import kotlin.test.assertEquals

class EventBusTest {

  private val plugin = SimplePluginContainer("test")


  @Test fun `test generic registration`(): Unit = runBlocking {
    val counter = LongAdder()

    EventBus.subscribe<TestEvent>(plugin) { event ->
      counter.add(event.value.toLong())
    }
    EventBus.post(TestEvent(100))

    assertEquals(100, counter.toInt())
  }

  @Test fun `test instance registration`(): Unit = runBlocking {
    val listeners = TestListeners()
    EventBus.subscribe(plugin, listeners)
    EventBus.post(TestEvent(100))

    delay(100) // Wait for async task

    assertEquals(3, listeners.counter.toInt())
  }

  class TestListeners {

    val counter = LongAdder()

    @Subscribe
    fun onTest(event: TestEvent) {
      this.counter.add(1)
    }

    @Subscribe
    suspend fun onSuspendTest(event: TestEvent) {
      this.counter.add(1)

      runAsync {
        EventBus.post(OtherEvent(2000))
      }
    }

    @Subscribe
    fun onOtherTest(event: OtherEvent) {
      this.counter.add(1)
    }
  }

  class TestEvent(val value: Int) : Event

  class OtherEvent(val value: Int) : Event

  class SimplePluginContainer(
      override val id: String,
      override val name: String = id,
      override val description: String? = null,
      override val authors: List<String> = emptyList(),
      override val instance: Any = Any()
  ) : PluginContainer
}
