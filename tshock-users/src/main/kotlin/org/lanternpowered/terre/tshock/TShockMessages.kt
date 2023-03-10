/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.tshock

object TShockMessages {

  private val authenticatedRegexes = sequenceOf(
    "Authenticated as {0} successfully.",
    "El jugador {0} ya está conectado.",
    "Vous avez identifié {0} avec succès.",
    "Berhasil diautentikasi sebagai {0}.",
    "Autenticado como {0} com sucesso.",
    "Вы залогинились как {0}.",
    "{0} adına giriş yapıldı.",
    "{0} 登录成功",
  ).map {
    it.replace(".", "\\.")
      .replace("{0}", "(.+)")
      .toRegex()
  }.toList()

  fun findAuthenticatedUser(message: String): String? =
    authenticatedRegexes.asSequence()
      .map { regex ->
        val match = regex.matchEntire(message)
        if (match != null) match.groupValues[1] else null
      }
      .filterNotNull()
      .firstOrNull()

  private val loggedOutMessages = setOf(
    "You are not logged-in. Therefore, you cannot logout.",
    "No has iniciado sesión. Por ende, no puedes cerrarla.",
    "Anda belum masuk. Oleh karena itu, anda tidak dapat keluar.",
    "Você não está conectado. Portanto, você não pode sair.",
    "Вы не вошли в аккаунт. Поэтому вы не можете выйти.",
    "你还没有登录所以不能登出。",
    "You have been successfully logged out of your account.",
    "Te desconectaste de tu cuenta exitosamente.",
    "Anda telah berhasil dalam mengeluarkan akun anda.",
    "Você foi desconectado da sua conta com sucesso.",
    "Вы успешно вышли из учётной записи.",
    "你已成功登出。",
  )

  fun isLoggedOut(message: String): Boolean = message in loggedOutMessages

  private val loggedOutSSCEnabledMessages = setOf(
    "Server side characters are enabled. You need to be logged-in to play.",
    "Los personajes exclusivos-de-servidor están activados. Debes estar autenticado para jugar.",
    "Karakter dalam peladen diaktifkan. anda harus masuk untuk bermain.",
    "Os personagens server side estão habilitados. Você precisa estar logado para jogar.",
    "Данные персонажей хранятся на сервере. Для игры вы должны войти в аккаунт.",
    "服务器角色存档（ssc）已启用。您需要登录才能游玩。",
  )

  fun isLoggedOutSSCEnabled(message: String): Boolean = message in loggedOutSSCEnabledMessages

  private val hasJoined = sequenceOf(
    "{0} has joined.",
    "{0} ist beigetreten.",
    "{0} se ha unido.",
    "{0} s'est connecté-e.",
    "{0} si è unito.",
    "{0} dołącza.",
    "{0} entrou.",
    "Игрок {0} присоединился.",
    "{0}已加入。",
  ).map {
    it.replace(".", "\\.")
      .replace("{0}", "(.+)")
      .toRegex()
  }.toList()

  fun isHasJoined(message: String): Boolean = hasJoined.any { it.matchEntire(message) != null }
}
