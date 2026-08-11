ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.4"

val kubernetesClientVersion = "7.8.0"

lazy val root = (project in file("."))
  .settings(
    name := "mcadm",
    libraryDependencies ++= Seq(
      "io.fabric8" % "kubernetes-client" % kubernetesClientVersion,
    ),
  )

lazy val libCrd = (project in file("lib/crd"))
  .settings(
    name := "lib-crd",
    libraryDependencies ++= Seq(
      "io.fabric8" % "kubernetes-client",
      "io.fabric8" % "generator-annotations"
    ).map(_ % kubernetesClientVersion),
    generateCRDs := {
      val outputDir = (Compile / resourceManaged).value
      CRDGen.generate(
        outputDir = outputDir,
        classpath = (Compile / fullClasspath).value.files,
        classesDirs = Seq((Compile / classDirectory).value)
      )
    },
    Compile / resourceGenerators += Def.task {
      (resourceManaged.value ** "*.yml").get
    }.taskValue,
  )

lazy val libDep = (project in file("lib/dep"))
  .settings(
    name := "lib-dep",
    libraryDependencies ++= Seq(
      "io.fabric8" % "kubernetes-client",
      "io.fabric8" % "generator-annotations"
    ).map(_ % kubernetesClientVersion),
  ).dependsOn(libCrd)

lazy val operator = (project in file("app/operator"))
  .settings(
    name := "app-operator"
  ).enablePlugins(DockerPlugin)
  .dependsOn(root)

lazy val generateCRDs = taskKey[Unit]("Generates CRDs using Fabric8 classes")
