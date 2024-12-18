package gg.scala.weblink.listener

import gg.scala.weblink.api.registration.RegistrationHandler
import gg.scala.weblink.api.user.UserApiHandler
import gg.scala.commons.annotations.Listeners
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@Listeners
object PlayerListener : Listener
{
    @EventHandler
    fun join(event: PlayerJoinEvent)
    {
        RegistrationHandler.validateRegisteredUser(event.player) {
            if (it)
            {
                UserApiHandler.connect(event.player)
            }
        }
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent)
    {
        RegistrationHandler.validateRegisteredUser(event.player) {
            if (it)
            {
                UserApiHandler.disconnect(event.player)
            }
        }
    }
}