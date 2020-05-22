
organization := "io.github.alexarchambault.sbt"

enablePlugins(ScriptedPlugin)
sbtPlugin := true
scriptedLaunchOpts += "-Dplugin.version=" + version.value
