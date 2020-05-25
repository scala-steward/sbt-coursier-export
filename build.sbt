
inThisBuild(List(
  organization := "io.github.alexarchambault.sbt",
  homepage := Some(url("https://github.com/alexarchambault/sbt-coursier-export")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "alexarchambault",
      "Alexandre Archambault",
      "",
      url("https://github.com/alexarchambault")
    )
  )
))

sonatypeProfileName := "io.github.alexarchambault"

enablePlugins(ScriptedPlugin)
sbtPlugin := true
scriptedLaunchOpts += "-Dplugin.version=" + version.value
