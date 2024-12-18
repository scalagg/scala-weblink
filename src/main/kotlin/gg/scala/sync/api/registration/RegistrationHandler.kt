package gg.scala.sync.api.registration

import gg.scala.sync.api.BASE_API_URL
import gg.scala.sync.api.EMAIL_REGEX
import gg.scala.sync.api.USER_AGENT
import gg.scala.commons.acf.ConditionFailedException
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

object RegistrationHandler
{

    @Throws(ConditionFailedException::class)
    fun attemptRegistration(player: Player, email: String)
    {
        if (!EMAIL_REGEX.matches(email))
        {
            throw ConditionFailedException("Use correct e-mail format, for example \"no-reply@scala.gg\"")
        }

        validateRegisteredUser(player) { registered ->
            if (registered)
            {
                throw ConditionFailedException("You're already registered!")
            }

            validateRegisteredEmail(email) { registeredEmail ->
                if (registeredEmail)
                {
                    throw ConditionFailedException("That email is already registered on our forums.")
                }

                try
                {
                    val connection = (URL(
                            "${
                                String.format(
                                        BASE_API_URL, "access"
                                )
                            }&email=$email&user=${player.name}&id=${player.uniqueId}"
                    ).openConnection() as HttpURLConnection).also {
                        it.requestMethod = "POST"
                        it.setRequestProperty("User-Agent", USER_AGENT)
                    }

                    var callback = ""

                    BufferedReader(
                            InputStreamReader(
                                    connection.inputStream
                            )
                    ).use { bufferedReader ->
                        while (bufferedReader.ready())
                        {
                            callback += bufferedReader.readLine() ?: ""
                        }
                    }

                    player.sendMessage("${ChatColor.GREEN}$callback")
                } catch (exception: Exception)
                {
                    exception.printStackTrace()
                }
            }
        }
    }

    fun validateRegisteredUser(player: Player, action: Consumer<Boolean>) =
        validateKeyValue("uuid", player.uniqueId.toString(), "userExist", action)

    fun validateRegisteredEmail(email: String, action: Consumer<Boolean>) =
        validateKeyValue("email", email, "emailExist", action)

    private fun validateKeyValue(key: String, value: String, endpoint: String, action: Consumer<Boolean>)
    {
        CompletableFuture.supplyAsync {
            try
            {
                val connection = (URL(
                        "${
                            String.format(
                                    BASE_API_URL, endpoint
                            )
                        }&$key=$value"
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
        }.thenAccept(action)
    }
}