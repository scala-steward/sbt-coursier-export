package sbtcoursierexport

import java.io.File
import java.nio.file.Files
import java.nio.charset.StandardCharsets

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.librarymanagement.ScalaModuleInfo

object SbtCoursierExportPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = JvmPlugin

  object autoImport {
    val coursierExport = taskKey[Seq[String]]("Prints dependencies / repositories / etc. as a list of arguments that can be passed to the coursier CLI")
    val coursierExportTo = inputKey[File]("Write to a file dependencies / repositories / etc. as a list of arguments that can be passed to the coursier CLI")
    val classPathExport = taskKey[String]("Prints the class path of the current module in a format that can be passed to 'java -cp'")
    val publishDependenciesLocal = taskKey[Unit]("Publishes locally the dependencies of the current module")
  }

  import autoImport._

  private def moduleName(crossVersion: CrossVersion, sv: String, sbv: String, baseName: String): String =
    CrossVersion(crossVersion, sv, sbv).fold(baseName)(_(baseName))
  private def moduleName(m: ModuleID, sv: String, sbv: String): String =
    moduleName(m.crossVersion, sv, sbv, m.name)

  private def writeTo: Def.Initialize[sbt.InputTask[File]] =
    Def.inputTask {
      import sbt.complete.DefaultParsers._

      val destPath = (OptSpace ~> StringBasic).parsed.trim
      val dest = new File(destPath)
      val content = coursierExport.value.mkString("\n")
      Files.write(dest.toPath, content.getBytes(StandardCharsets.UTF_8))
      dest
    }

  override def projectSettings = Def.settings(
    coursierExport := {

      val log = streams.value.log

      val sv = scalaVersion.value
      val sbv = scalaBinaryVersion.value

      val scalaVer = scalaModuleInfo.value.toSeq.map { info =>
        Seq("--scala-version", info.scalaFullVersion)
      }

      val deps = allDependencies.value.map { m =>
        val excl = m.exclusions.map { e =>
          s",exclude=${e.organization}%${moduleName(e.crossVersion, sv, sbv, e.name)}"
        }
        val sepOpt =
          if (m.crossVersion == CrossVersion.binary) Some("::")
          else if (m.crossVersion == CrossVersion.full || m.crossVersion == CrossVersion.patch) Some(":::")
          else if (m.crossVersion == CrossVersion.disabled) Some(":")
          else None
        val dep = sepOpt match {
          case None =>
            s"${m.organization}:${moduleName(m, sv, sbv)}:${m.revision}${excl.mkString}"
          case Some(sep) =>
            s"${m.organization}$sep${m.name}:${m.revision}${excl.mkString}"
        }
        Seq(dep)
      }

      val forceVersions = dependencyOverrides.value.map { m =>
        Seq("-V", s"${m.organization}:${moduleName(m, sv, sbv)}:${m.revision}")
      }

      val excludes = excludeDependencies.value.map { rule =>
        if (rule.artifact != "*")
          log.warn(s"Unsupported artifact value '${rule.artifact}' in exclusion rule $rule")
        if (rule.configurations.nonEmpty)
          log.warn(s"Unsupported configurations value '${rule.configurations.mkString(", ")}' in exclusion rule $rule")

        Seq("-E", s"${rule.organization}:${moduleName(rule.crossVersion, sv, sbv, rule.name)}")
      }

      val ivyProperties = Resolvers.defaultIvyProperties(ivyPaths.value.ivyHome)
      val repositories = fullResolvers.value.flatMap { resolver =>
        Resolvers.repository(resolver, ivyProperties, log).toSeq.map(repo => Seq("-r", repo))
      }

      // TODO versionReconciliation from sbt-lm-coursier?

      val argGroups = deps ++
        forceVersions ++
        excludes ++
        scalaVer ++
        Seq(Seq("--no-default")) ++
        repositories

      System.err.println(argGroups.flatten.mkString(System.lineSeparator))

      argGroups.flatten
    },
    coursierExportTo := writeTo.evaluated,
    classPathExport := {
      val classPathSeq = fullClasspathAsJars.in(Compile).value
      val classPath = classPathSeq
        .map(_.data.getAbsolutePath)
        .mkString(File.pathSeparator)
      System.err.println(classPath)
      classPath
    },
    publishDependenciesLocal := Def.taskDyn {
      import Structure._

      val state0 = state.value
      val projectRef = thisProjectRef.value
      val projects = allRecursiveInterDependencies(state0, projectRef)

      val task = publishLocal.forAllProjects(state0, projects)
      Def.task(task.value)
    }.value
  )

}
