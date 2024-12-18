package gg.scala.weblink.api.registration

import gg.scala.weblink.api.BASE_API_URL
import gg.scala.weblink.api.EMAIL_REGEX
import gg.scala.weblink.api.USER_AGENT
import gg.scala.commons.acf.ConditionFailedException
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

object RegistrationHandler {

    @Throws(ConditionFailedException::class)
    fun attemptRegistration(player: Player, email: String)
    {
        if (!EMAIL_REGEX.matches(email)) {
            throw ConditionFailedException("Use correct e-mail format, for example \"admin@scala.gg\"")
        }

        validateRegisteredUser(player) { registered ->
            if (registered) {
                throw ConditionFailedException("You're already registered!")
            }

            validateRegisteredEmail(email) { registeredEmail ->
                if (registeredEmail) {
                    throw ConditionFailedException("That email is already registered on our forums.")
                }

                try {
                    val url = "${
                        String.format(BASE_API_URL, "access")
                    }&email=$email&user=${player.name}&id=${player.uniqueId}"
                    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("User-Agent", USER_AGENT)
                    }

                    val responseCode = connection.responseCode
                    val responseMessage = connection.responseMessage

                    println("Response Code: $responseCode")
                    println("Response Message: $responseMessage")

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        player.sendMessage("${ChatColor.RED}Registration failed: $responseMessage")
                        return@validateRegisteredEmail
                    }

                    val callback = connection.inputStream.bufferedReader().use { it.readText() }
                    println("API Callback Response: $callback")
                    player.sendMessage("${ChatColor.GREEN}$callback")
                } catch (ioException: IOException) {
                    player.sendMessage("${ChatColor.RED}Network error occurred. Please try again later.")
                    ioException.printStackTrace()
                } catch (exception: Exception) {
                    player.sendMessage("${ChatColor.RED}An unexpected error occurred.")
                    exception.printStackTrace()
                }
            }
        }
    }

    fun validateRegisteredUser(player: Player, action: Consumer<Boolean>) =
        validateKeyValue("uuid", player.uniqueId.toString(), "userExist", action)

    fun validateRegisteredEmail(email: String, action: Consumer<Boolean>) =
        validateKeyValue("email", email, "emailExist", action)

    private fun validateKeyValue(key: String, value: String, endpoint: String, action: Consumer<Boolean>) {
        CompletableFuture.supplyAsync {
            try {
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
                    while (bufferedReader.ready()) {
                        exists += bufferedReader.readLine() ?: ""
                    }
                }

                return@supplyAsync exists == "exists"
            } catch (exception: Exception) {
                exception.printStackTrace()
                return@supplyAsync false
            }
        }.thenAccept(action)
    }
}