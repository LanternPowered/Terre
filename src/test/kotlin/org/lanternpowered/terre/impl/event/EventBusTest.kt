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

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.lanternpowered.terre.dispatcher.launchAsync
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.event.EventBus
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.subscribe
import org.lanternpowered.terre.impl.plugin.TerrePluginContainer
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.plugin.withActivePlugin
import java.util.concurrent.atomic.LongAdder
import kotlin.test.assertEquals

class EventBusTest {

  private val plugin = TerrePluginContainer(id = "test", instance = Any())

  @Test fun `test active plugin`(): Unit = runBlocking {
    val counter = LongAdder()
    withActivePlugin(plugin) {
      EventBus.subscribe<TestEvent> {
        assertEquals(plugin, PluginContainer.Active)
        counter.increment()
      }
    }
    EventBus.post(TestEvent)
    assertEquals(1, counter.toInt())
  }

  @Test fun `test active plugin - in async task`(): Unit = runBlocking {
    val counter = LongAdder()
    withActivePlugin(plugin) {
      EventBus.subscribe<TestEvent> {
        launchAsync {
          assertEquals(plugin, PluginContainer.Active)
          counter.increment()
        }.join()
      }
    }
    EventBus.post(TestEvent)
    assertEquals(1, counter.toInt())
  }

  @Test fun `test generic registration`(): Unit = runBlocking {
    val counter = LongAdder()
    withActivePlugin(plugin) {
      EventBus.subscribe<TestEvent> {
        counter.add(1)
        assertEquals(plugin, PluginContainer.Active)
      }
    }
    EventBus.post(TestEvent)
    assertEquals(1, counter.toInt())
  }

  @Test fun `test instance registration`(): Unit = runBlocking {
    val listeners = TestListeners()
    withActivePlugin(plugin) {
      EventBus.register(listeners)
    }
    EventBus.post(TestEvent)
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

      launchAsync {
        EventBus.post(OtherEvent(2000))
      }.join()
    }

    @Subscribe
    fun onOtherTest(event: OtherEvent) {
      this.counter.add(1)
    }
  }

  object TestEvent : Event

  class OtherEvent(val value: Int) : Event
}
