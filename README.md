# sbt-coursier-export

A plugin to get your dependencies, repositories, etc. as arguments that can be
passed to the [coursier CLI](https://get-coursier.io/docs/cli-overview).

## Usage

Add to `project/plugins.sbt`
```scala
addSbtPlugin("io.github.alexarchambault.sbt" % "sbt-coursier-export" % "0.1.0")
```

Then run the `coursierExport` task from a module, like
```text
$ sbt
> some-project/coursierExport
org.scala-lang:scala-compiler:2.12.11
org.scala-lang:scala-library:2.12.11
org.scalamacros:::paradise:2.1.1
--scala-version 2.12.11
--no-default
-r ivy:file:///Users/foo/.ivy2//local/[organisation]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)([branch]/)[revision]/[type]s/[artifact](-[classifier]).[ext]|file:///Users/foo/.ivy2//local/[organisation]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)([branch]/)[revision]/[type]s/[artifact](-[classifier]).[ext]
-r https://repo1.maven.org/maven2/
-r ivy:https://repo.typesafe.com/typesafe/ivy-releases/[organization]/[module]/[revision]/[type]s/[artifact](-[classifier]).[ext]|https://repo.typesafe.com/typesafe/ivy-releases/[organization]/[module]/[revision]/[type]s/[artifact](-[classifier]).[ext]
-r ivy:https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/[organization]/[module](/scala_[scalaVersion])(/sbt_[sbtVersion])/[revision]/[type]s/[artifact](-[classifier]).[ext]|https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/[organization]/[module](/scala_[scalaVersion])(/sbt_[sbtVersion])/[revision]/[type]s/[artifact](-[classifier]).[ext]
```

## Note

The exported arguments do not originate from coursier itself, that runs the resolution. Rather, they are a best effort at
reconstructing coursier inputs from various sbt keys. It's still possible that some parameters may be missing. So there can
still be discrepancies between how resolution is run from sbt, and how it's run via the arguments exported by sbt-coursier-export, if your build uses features or keys not yet supported by sbt-coursier-export.
