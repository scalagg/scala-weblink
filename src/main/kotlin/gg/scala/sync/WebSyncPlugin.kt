package gg.scala.sync

import gg.scala.sync.api.user.UserApiHandler
import gg.scala.sync.model.WebsitePlayerScheme
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.*
import io.github.nosequel.data.DataHandler
import io.github.nosequel.data.connection.mongo.NoAuthMongoConnectionPool

import org.bukkit.Bukkit

@Plugin(
    name = "WebSync",
    version = "%remote%/%branch%/%id%"

)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Cookie", soft = true),
    PluginDependency("cloudsync", soft = true)
)
class WebSyncPlugin : ExtendedScalaPlugin()
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