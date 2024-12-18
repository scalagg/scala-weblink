package gg.scala.weblink.model

import gg.scala.lemon.LemonConstants
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.store.storage.storable.IDataStoreObject
import io.github.nosequel.data.DataHandler
import io.github.nosequel.data.DataStoreType
import java.util.*

data class WebsitePlayerScheme(
        var username: String,
        var email: String,
        val uuid: String,
        var rank: String = "Default",
        var lastLogin: Long = System.currentTimeMillis(),
        var lastServer: String = "unknown",
        var bio: String = "No bio set.",
        var online: Boolean = false,
        var banned: Boolean = false,
        var blacklisted: Boolean = false,
        var maintainer: Boolean = false,

        // these fields are all stored by the website. we don't have to do anything with them, considering
        // we just have them here to re-store them whenever we have to.
        var password: String,
        var connections: List<String> = mutableListOf(),
) : IDataStoreObject
{
    override val identifier: UUID = UUID.fromString(uuid)

    companion object
    {
        private val schemes = hashMapOf<UUID, WebsitePlayerScheme>()
        val data by lazy {
            DataHandler.createStoreType<UUID, WebsitePlayerScheme>(
                    DataStoreType.MONGO
            )
        }

        fun convert(lemonPlayer: LemonPlayer): WebsitePlayerScheme
        {
            val oldScheme = data
                    .retrieve(lemonPlayer.identifier)
                    ?: throw IllegalStateException("Unable to load player ${lemonPlayer}, because the player hasn't been registered yet.")

            return WebsitePlayerScheme(
                    username = lemonPlayer.name,
                    email = oldScheme.email,
                    password = oldScheme.password,
                    uuid = lemonPlayer
                            .uniqueId
                            .toString(),
            ).apply {
                schemes[lemonPlayer.identifier] = this
            }
        }

        operator fun get(uniqueId: UUID): WebsitePlayerScheme?
        {
            return schemes[uniqueId] ?: data.retrieve(uniqueId)
        }
    }

    fun store()
    {
        println("saving to mongo!")
        data.store(UUID.fromString(uuid), this)
    }

    fun disconnect(lemonPlayer: LemonPlayer)
    {
        updateFromlemonPlayer(lemonPlayer).apply {
            online = false
        }
    }

    fun updateFromlemonPlayer(lemonPlayer: LemonPlayer)
    {
        this.rank = lemonPlayer.activeGrant?.getRank()?.name ?: "Default"
        this.lastLogin = System.currentTimeMillis()
        this.lastServer = LemonConstants.SERVER_NAME
        this.online = true

        this.banned = lemonPlayer.activePunishments[PunishmentCategory.BAN]?.isActive ?: false
        this.blacklisted = lemonPlayer.activePunishments[PunishmentCategory.BLACKLIST]?.isActive ?: false

        this.maintainer = lemonPlayer
                .bukkitPlayer
                ?.hasPermission(
                        "mommy.mommy.emily"
                ) ?: this.maintainer
    }
}