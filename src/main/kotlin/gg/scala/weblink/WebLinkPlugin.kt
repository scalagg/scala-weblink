package gg.scala.weblink

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.core.plugin.*

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
class WebLinkPlugin : ExtendedScalaPlugin() { }