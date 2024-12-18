package gg.scala.weblink.api.user

import gg.scala.weblink.api.BASE_API_URL
import gg.scala.weblink.api.USER_AGENT
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

object UserApiHandler
{
    val updateMap = hashMapOf<String, (Player) -> Unit>(
            "updateLogin" to {
                updatePlayerField(it, "lastLogin", System.currentTimeMillis().toString(), "updateLogin")
            },
            "updateServer" to {
                updatePlayerField(it, "server", Lemon.instance.settings.id, "updateServer")
            },
            "updaterank" to { player ->
                val lemonPlayerPlayer = PlayerHandler.findPlayer(player)

                lemonPlayerPlayer.ifPresent {
                    val grant = it.activeGrant ?: return@ifPresent
                    val rank = grant.getRank()

                    updatePlayerField(
                            player, "rank", rank.name.lowercase(), "updaterank"
                    )
                }
            },
            "banned" to { player ->
                val lemonPlayerPlayer = PlayerHandler.findPlayer(player)

                lemonPlayerPlayer.ifPresent {
                    val punishment = it.activePunishments[PunishmentCategory.BAN] ?: return@ifPresent

                    if (punishment.isActive)
                    {
                        updatePlayerField(
                                player, "banned", "true", "banned"
                        )
                    }
                }
            },
            "blacklisted" to { player ->
                val lemonPlayerPlayer = PlayerHandler.findPlayer(player)

                lemonPlayerPlayer.ifPresent {
                    val punishment = it.activePunishments[PunishmentCategory.BLACKLIST] ?: return@ifPresent

                    if (punishment.isActive)
                    {
                        updatePlayerField(
                                player, "blacklisted", "true", "blacklisted"
                        )
                    }
                }
            },
            "unbanned" to { player ->
                val lemonPlayerPlayer = PlayerHandler.findPlayer(player)

                lemonPlayerPlayer.ifPresent {
                    val punishment = it.activePunishments[PunishmentCategory.BAN]

                    if (punishment == null || !punishment.isActive)
                    {
                        updatePlayerField(
                                player, "unban", "true", "unbanned"
                        )
                    }
                }
            },
            "unblacklisted" to { player ->
                val lemonPlayerPlayer = PlayerHandler.findPlayer(player)

                lemonPlayerPlayer.ifPresent {
                    val punishment = it.activePunishments[PunishmentCategory.BLACKLIST]

                    if (punishment == null || !punishment.isActive)
                    {
                        updatePlayerField(
                                player, "unblacklisted", "true", "unblacklisted"
                        )
                    }
                }
            },
    )

    fun updatePlayer(player: Player, map: HashMap<String, (Player) -> Unit> = updateMap)
    {
        map.forEach {
            it.value(player)
        }
    }

    fun connect(player: Player)
    {
        val map = hashMapOf<String, (Player) -> Unit>()

        updateMap.forEach {
            map[it.key] = it.value
        }

        map["online"] = {
            updatePlayerField(it, "online", "true", "online")
        }

        updatePlayer(player, map)
    }

    fun disconnect(player: Player)
    {
        val map = hashMapOf<String, (Player) -> Unit>()

        updateMap.forEach {
            map[it.key] = it.value
        }

        map["offline"] = {
            updatePlayerField(it, "offline", "true", "offline")
        }

        updatePlayer(player, map)
    }

    fun updatePlayerField(player: Player, fieldKey: String, fieldValue: String, endpointType: String)
    {
        CompletableFuture.supplyAsync {
            try
            {
                val connection = (URL(
                        "${
                            String.format(
                                    BASE_API_URL, endpointType
                            )
                        }&uuid=${player.uniqueId}&${fieldKey}=${fieldValue}"
                ).openConnection() as HttpURLConnection).also {
                    it.requestMethod = "POST"
                    it.setRequestProperty("User-Agent", USER_AGENT)
                }

                var exists = ""

                BufferedReader(
                        InputStreamReader(
                                connection.inputStream
                        )
                ).use { bufferedReader ->
                    while (bufferedReader.ready())
                    {
                        exists += bufferedReader.readLine() ?: ""
                    }
                }

                return@supplyAsync exists == "exists"
            } catch (exception: Exception)
            {
                exception.printStackTrace()
                return@supplyAsync false
            }
        }
    }
}