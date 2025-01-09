package gg.scala.weblink.command

import gg.scala.weblink.registration.RegistrationHandler
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.Syntax
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

@AutoRegister
object RegisterCommand : ScalaCommand()
{
    private const val RATE_LIMIT_DURATION = 120 * 1000 // 2 minutes in milliseconds
    private val rateLimitMap = hashMapOf<UUID, Long>()
    private val logger: Logger = Logger.getLogger("RegisterCommand")

    @CommandAlias("register")
    @Syntax("<email>")
    @Description("Create a website account")
    fun register(player: Player, email: String)
    {
        val playerId = player.uniqueId
        if (rateLimitMap.containsKey(playerId)) {
            val lastAttempt = rateLimitMap[playerId]!!
            if (System.currentTimeMillis() - lastAttempt < RATE_LIMIT_DURATION) {
                player.sendMessage("You are registering too quickly. Please wait a moment before trying again.")
                return
            }
        }

        try {
            rateLimitMap[playerId] = System.currentTimeMillis()

            CompletableFuture.runAsync {
                RegistrationHandler.attemptRegistration(player, email)
            }
        } catch (exception: Exception) {
            logger.severe("Registration failed for player ${player.name} with email: $email. Error: ${exception.message}")
            throw ConditionFailedException("Registration failed. Please try again later.")
        }
    }
}