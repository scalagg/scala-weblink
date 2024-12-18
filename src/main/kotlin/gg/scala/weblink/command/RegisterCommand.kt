package gg.scala.weblink.command

import gg.scala.weblink.api.registration.RegistrationHandler
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.Syntax
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import org.bukkit.entity.Player
import java.util.UUID
import java.util.logging.Logger

@AutoRegister
object RegisterCommand : ScalaCommand()
{
    private const val RATE_LIMIT_DURATION = 120 * 1000 // 2 minutes in milliseconds
    private val rateMap = hashMapOf<UUID, Int>()
    private val rateLimitMap = hashMapOf<UUID, Long>()
    private val logger: Logger = Logger.getLogger("RegisterCommand")

    @CommandAlias("register")
    @Syntax("<email>")
    @Description("Create a website account")
    fun register(player: Player, email: String)
    {
        val playerId = player.uniqueId

        rateMap[playerId] = (rateMap[playerId] ?: 0) + 1

        if (rateMap[playerId]!! >= 5) {
            rateLimitMap[playerId] = System.currentTimeMillis()
        }

        if (rateLimitMap.containsKey(playerId)) {
            val rateLimitStart = rateLimitMap[playerId]!!
            val remainingTime = rateLimitStart + RATE_LIMIT_DURATION - System.currentTimeMillis()

            if (remainingTime > 0) {
                val minutes = (remainingTime / 1000) / 60
                val seconds = (remainingTime / 1000) % 60
                val errorMessage = "You've been rate limited! ${minutes}m, ${seconds}s remaining."

                logger.warning("Player ${player.name} tried to register but was rate-limited. $errorMessage")
                throw ConditionFailedException(errorMessage)
            } else {
                rateLimitMap.remove(playerId)
                rateMap[playerId] = 0
            }
        }

        try {
            RegistrationHandler.attemptRegistration(player, email)
        } catch (exception: Exception) {
            logger.severe("Registration failed for player ${player.name} with email: $email. Error: ${exception.message}")
            throw ConditionFailedException("Registration failed. Please try again later.")
        }
    }
}