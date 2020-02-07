package org.lanternpowered.terre.logger

import org.apache.logging.log4j.Logger

interface Logger : Logger {

  fun debug(fn: () -> String)

  fun warn(fn: () -> String)
}
