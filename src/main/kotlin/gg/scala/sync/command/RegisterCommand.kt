package gg.scala.sync.command

import gg.scala.sync.api.registration.RegistrationHandler
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import org.bukkit.entity.Player
import java.util.UUID
import java.util.logging.Logger

@AutoRegister
object RegisterCommand : ScalaCommand()
{
    private const val RATE_LIMIT_DURATION = 120 * 1000
    private val rateMap = hashMapOf<UUID, Int>()
    private val rateLimitMap = hashMapOf<UUID, Long>()
    private val logger: Logger = Logger.getLogger("RegisterCommand")

    @CommandAlias("register")
    fun register(player: Player, email: String)
    {
        rateMap.putIfAbsent(player.uniqueId, 0)
        rateMap[player.uniqueId]!!.inc()

        if (rateMap[player.uniqueId]!! == 5)
        {
            rateLimitMap[player.uniqueId] = System.currentTimeMillis()
        }

        if (rateLimitMap.containsKey(player.uniqueId))
        {
            val value = rateLimitMap[player.uniqueId]!!

            if (value + RATE_LIMIT_DURATION > System.currentTimeMillis())
            {
                var seconds = value / 1000
                val minutes = seconds / 60
                seconds = seconds
                    .mod(60)
                    .toLong()

                val errorMessage = "You've been rate limited! ${minutes}m, ${seconds}s"
                // Log the error in the console
                logger.warning("Player ${player.name} tried to register but was rate-limited. $errorMessage")
                // Throw the exception with the detailed message
                throw ConditionFailedException(errorMessage)
            }
        }

        try {
            RegistrationHandler.attemptRegistration(player, email)
        } catch (e: Exception) {
            // Log the failure in the console with details
            logger.severe("Registration failed for player ${player.name} with email: $email. Error: ${e.message}")
            throw ConditionFailedException("Registration failed. Please check the console for details.")
        }
    }
}
