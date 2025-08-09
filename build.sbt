import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.2"

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

lazy val root = (project in file("."))
  .settings(
    name                            := "pme123-windspotter",
    sourcesInBase                   := false,
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("pme123-windspotter"))
        )
    },
    libraryDependencies ++= Seq(
      "com.raquo"   %%% "laminar"            % "17.2.0",
      "be.doeraene" %%% "web-components-ui5" % "2.1.0"
    )
  ).enablePlugins(ScalaJSPlugin)
