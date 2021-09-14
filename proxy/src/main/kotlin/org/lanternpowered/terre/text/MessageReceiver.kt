/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.text

/**
 * Represents something that can receive messages.
 */
interface MessageReceiver {

  /**
   * Sends a text message.
   */
  fun sendMessage(message: TextLike) =
    sendMessage(message.text())

  /**
   * Sends a text message.
   */
  fun sendMessage(message: Text)

  /**
   * Sends a text message as if it was sent by the given [MessageSender].
   *
   * This will prepend '<sender>' to the message, where sender is the name of the sender.
   *
   * If the sender and receiver are on the same server and within visible range the receiver will
   * see a chat balloon above the sender' head.
   */
  fun sendMessageAs(message: TextLike, sender: MessageSender) =
    sendMessageAs(message.text(), sender)

  /**
   * Sends a text message as if it was sent by the given [MessageSender].
   *
   * This will prepend '<sender>' to the message, where sender is the name of the sender.
   *
   * If the sender and receiver are on the same server and within visible range the receiver will
   * see a chat balloon above the sender' head.
   */
  fun sendMessageAs(message: Text, sender: MessageSender)

  /**
   * Sends a plain message.
   */
  fun sendMessage(message: String) =
    sendMessage(message.text())

  /**
   * Sends a plain message as if it was sent by the given [MessageSender].
   *
   * This will prepend '<sender>' to the message, where sender is the name of the sender.
   *
   * If the sender and receiver are on the same server and within visible range the receiver will
   * see a chat balloon above the sender' head.
   */
  fun sendMessageAs(message: String, sender: MessageSender) =
    sendMessageAs(message.text(), sender)
}
