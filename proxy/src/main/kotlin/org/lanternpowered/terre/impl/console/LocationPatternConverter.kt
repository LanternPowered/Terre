/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.console

import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.pattern.ConverterKeys
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter
import org.apache.logging.log4j.core.pattern.PatternConverter
import org.apache.logging.log4j.io.LoggerPrintStream
import org.lanternpowered.terre.impl.logger.LoggerImpl
import java.io.PrintStream
import java.util.regex.Matcher

@ConverterKeys("loc")
@Plugin(name = "LocationPatternConverter", category = PatternConverter.CATEGORY)
internal class LocationPatternConverter private constructor(
  private val format: String
) : LogEventPatternConverter("Location", "location") {

  // Packages that will be ignored
  private val ignoredPackages = arrayOf("java.", "kotlin.io.")

  override fun format(event: LogEvent, builder: StringBuilder) {
    val element = calculateLocation(event.loggerFqcn)
    if (element != null) {
      // quoteReplacement is required for elements leading to inner class (containing a $ character)
      builder.append(format.replace("%path".toRegex(),
        Matcher.quoteReplacement(element.toString())))
    }
  }

  private fun calculateLocation(fqcn: String): StackTraceElement? {
    val stackTrace = Throwable().stackTrace
    var last: StackTraceElement? = null

    for (i in stackTrace.size - 1 downTo 1) {
      val className = stackTrace[i].className
      // Check if the target logger source should be redirected
      if (RedirectFqcns.contains(className) || className == fqcn)
        return last
      // Check if the target logger source should be ignored
      if (IgnoreFqcns.contains(className))
        return null
      // Reaching the printStackTrace method is also the end of the road
      if (className == "java.lang.Throwable" && stackTrace[i].methodName == "printStackTrace")
        return null
      // Ignore Kotlin and Java packages
      if (ignoredPackages.none { className.startsWith(it) })
        last = stackTrace[i]
    }

    return null
  }

  companion object {

    val RedirectFqcns = hashSetOf(
      PrintStream::class.java.name,
      LoggerPrintStream::class.java.name,
      LoggerImpl::class.java.name
    )

    val IgnoreFqcns = hashSetOf<String>()

    @JvmStatic
    fun newInstance(options: Array<String>): LocationPatternConverter =
      LocationPatternConverter(if (options.isNotEmpty()) options[0] else "%path")
  }
}
