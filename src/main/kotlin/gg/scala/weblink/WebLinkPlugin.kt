package gg.scala.weblink

import gg.scala.weblink.api.user.UserApiHandler
import gg.scala.weblink.model.WebsitePlayerScheme
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.*
import io.github.nosequel.data.DataHandler
import io.github.nosequel.data.connection.mongo.NoAuthMongoConnectionPool

import org.bukkit.Bukkit

@Plugin(
    name = "scala-weblink",
    version = "%remote%/%branch%/%id%"

)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("cloudsync", soft = true)
)
class WebLinkPlugin : ExtendedScalaPlugin()
{

    @ContainerEnable
    fun onContainerEnable()
    {
        DataHandler
                .linkTypeToId<WebsitePlayerScheme>("users")
                .withConnectionPool<NoAuthMongoConnectionPool> {
                    this.hostname = "127.0.0.1"
                    this.port = 27017
                    this.databaseName = "fynda"
                }
    }

    @ContainerDisable
    fun onContainerDisable()
    {
        Bukkit
                .getOnlinePlayers()
                .forEach {
                    UserApiHandler.disconnect(it)
                }
    }
}