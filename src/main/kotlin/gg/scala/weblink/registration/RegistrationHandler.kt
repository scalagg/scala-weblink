package gg.scala.weblink.registration

import gg.scala.weblink.api.API_URL
import gg.scala.weblink.api.EMAIL_REGEX
import gg.scala.weblink.api.USER_AGENT
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.weblink.api.API_TOKEN
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object RegistrationHandler {

    @Throws(ConditionFailedException::class)
    fun attemptRegistration(player: Player, email: String)
    {
        if (!EMAIL_REGEX.matches(email)) {
            throw ConditionFailedException("Use correct e-mail format, for example \"admin@scala.gg\"")
        }

        try {
            val url = API_URL.format(player.uniqueId)
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", API_TOKEN)

                val token = UUID.randomUUID().toString().substring(0, 8)
                val body = "{\"email\":\"$email\",\"token\":\"${token}\"}"
                doOutput = true
                outputStream.use { it.write(body.toByteArray()) }
            }

            val responseCode = connection.responseCode

            if (responseCode == 400) {
                // account already exists
                player.sendMessage("${ChatColor.RED}It looks like you already have an account setup.")
                return
            }

            if (responseCode == 401) {
                // email already exists
                player.sendMessage("${ChatColor.RED}It looks like there is already an account with that email, try again with a new email.")
                return
            }

            println("Response Code: $responseCode")

            if (responseCode != HttpURLConnection.HTTP_OK) {
                player.sendMessage("${ChatColor.RED}Something went wrong, try re-registering and if the issue persists contact an admin.")
                return
            }

            player.sendMessage("${ChatColor.GREEN}You have been sent an email with a login token. You can use it to make an account by following the instructions you received.")
        } catch (ioException: IOException) {
            player.sendMessage("${ChatColor.RED}Network error occurred. Please try again later.")
            ioException.printStackTrace()
        } catch (exception: Exception) {
            player.sendMessage("${ChatColor.RED}An unexpected error occurred.")
            exception.printStackTrace()
        }

    }

}