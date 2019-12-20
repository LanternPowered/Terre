/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.util.executor

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledFuture

/**
 * A scheduled completable future.
 */
abstract class ScheduledCompletableFuture<T> : CompletableFuture<T>(), ScheduledFuture<T>
