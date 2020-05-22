
lazy val a = project
  .settings(
    shared,
    libraryDependencies ++= Seq(
      "io.github.alexarchambault" %% "data-class" % "0.2.3"
    )
  )

lazy val b = project
  .dependsOn(a)
  .settings(
    shared,
    libraryDependencies ++= Seq(
      "eu.timepit" %% "refined" % "0.9.14",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
    )
  )

inThisBuild(List(
  scalaVersion := "2.12.11",
  organization := "io.github.alexarchambault.sbt.test",
  version := "0.1.0"
))

lazy val check = taskKey[Unit]("")
lazy val shared = Def.settings(
  check := {
    val ivyHome = new File(System.getProperty("user.home")).toURI.getPath + ".ivy2"
    val args = coursierExport.value.map(_.replace(ivyHome, "IVY2-HOME"))
    val expectedArgs = Seq(
      "org.scala-lang:scala-compiler:2.12.11",
      "org.scala-lang:scala-library:2.12.11",
      "io.github.alexarchambault.sbt.test::a:0.1.0",
      "org.scala-lang:scala-library:2.12.11",
      "eu.timepit::refined:0.9.14",
      "org.scalamacros:::paradise:2.1.1",
      "--scala-version", "2.12.11",
      "--no-default",
      "-r", "ivy:file://IVY2-HOME//local/[organisation]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)([branch]/)[revision]/[type]s/[artifact](-[classifier]).[ext]|file://IVY2-HOME//local/[organisation]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)([branch]/)[revision]/[type]s/[artifact](-[classifier]).[ext]",
      "-r", "https://repo1.maven.org/maven2/",
      "-r", "ivy:https://repo.typesafe.com/typesafe/ivy-releases/[organization]/[module]/[revision]/[type]s/[artifact](-[classifier]).[ext]|https://repo.typesafe.com/typesafe/ivy-releases/[organization]/[module]/[revision]/[type]s/[artifact](-[classifier]).[ext]",
      "-r", "ivy:https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/[organization]/[module](/scala_[scalaVersion])(/sbt_[sbtVersion])/[revision]/[type]s/[artifact](-[classifier]).[ext]|https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/[organization]/[module](/scala_[scalaVersion])(/sbt_[sbtVersion])/[revision]/[type]s/[artifact](-[classifier]).[ext]"
    )
    assert(args == expectedArgs, s"Expected\n${expectedArgs.map(_ + "\n").mkString}\n, got\n${args.map(_ + "\n").mkString}")
  }
)
