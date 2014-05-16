import sbt._
import Keys._
import java.io.File
import com.joshcough.minecraft.Plugin._
import sbtassembly.Plugin._
import AssemblyKeys._

object build extends Build {

  val projectUrl = "https://github.com/joshcough/NetLogoMinecraft"

  lazy val standardSettings = join(
    Defaults.defaultSettings,
    bintray.Plugin.bintraySettings,
    libDeps(
      "javax.servlet"      % "servlet-api" % "2.5"        % "provided->default",
      "org.scalacheck"    %% "scalacheck"  % "1.11.3"     % "test",
      "org.bukkit"         % "bukkit" % "1.7.2-R0.2",
      "com.joshcough"     %% "scala-minecraft-plugin-api" % "0.3.3"
    ),
    Seq(
      organization := "com.joshcough",
      version := "0.3.3",
      scalaVersion := "2.11.0",
      licenses <++= version(v => Seq("MIT" -> url(projectUrl + "/blob/%s/LICENSE".format(v)))),
      publishMavenStyle := true,
      resolvers += Resolver.sonatypeRepo("snapshots"),
      resolvers += "Bukkit" at "http://repo.bukkit.org/content/repositories/releases"
    )
  )

  // this is the main project, that builds all subprojects.
  // it doesnt contain any code itself.
  lazy val all = Project(
    id = "all",
    base = file("."),
    settings = standardSettings,
    aggregate = Seq(netlogoPlugin, netlogoLibPlugin)
  )

  lazy val npcLib = "com.joshcough" %% "remote-entities" % "1.7.2-R0.2_0.3.3"
  lazy val netlogoRepo = bintray.Opts.resolver.repo("netlogo", "NetLogoHeadless")
  lazy val remoteEntitiesRepo = bintray.Opts.resolver.repo("joshcough", "remote-entities")

  lazy val netlogoPlugin = Project(
    id = "netLogoPlugin",
    base = file("netlogoPlugin"),
    settings = join(
      standardSettings,
      copyPluginToBukkitSettings(None),
      pluginYmlSettings("com.joshcough.minecraft.NetLogoPlugin", "JoshCough"),
      libDeps(
        "org.nlogo" % "netlogoheadless" % "5.2.0-439e6c4",
        npcLib
      ),
      Seq(resolvers ++= Seq(netlogoRepo, remoteEntitiesRepo))
    )
  )

  lazy val netlogoLibPlugin = Project(
    id = "netLogoLibPlugin",
    base = file("netlogoLibPlugin"),
    settings = join(
      standardSettings,
      assemblySettings,
      mergeStrategy in assembly <<= (mergeStrategy in assembly) { old => {
        case "plugin.yml" => MergeStrategy.first
        case x => old(x)
      }},
      named("netlogo-lib-plugin"),
      copyPluginToBukkitSettings(Some("assembly")),
      libDeps(
        "org.nlogo" % "netlogoheadless" % "5.2.0-439e6c4",
        "asm" % "asm-all" % "3.3.1",
        "org.picocontainer" % "picocontainer" % "2.13.6",
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
      ),
      resolvers += netlogoRepo
    )
  )
}
