import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example.avro-meta-test",
      scalaVersion := "2.11.11",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % "1.7.7",
      "org.scalameta" %% "scalameta" % "1.8.0" % Provided,
      scalaTest % Test
    ),
    initialCommands in console :=
      """
        |import java.io.File
        |import org.apache.avro._
        |val parser = new Schema.Parser()
        |val schema = parser.parse(new File("schema/example.avsc"))
        |import avro._
        |
      """.stripMargin,
    macroAnnotationSettings
  ).dependsOn(avro_meta)

lazy val macroAnnotationSettings = Seq(
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise")) // macroparadise plugin doesn't work in repl yet.
)

lazy val avro_meta = (project in file("avro-meta")).
  settings(
    inThisBuild(List(
      organization := "com.example.avro-meta",
      scalaVersion := "2.11.11",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "avro-meta",
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % "1.7.7",
      "org.scalameta" %% "scalameta" % "1.8.0" % Provided,
      scalaTest % Test
    ),
    initialCommands in console :=
      """
        |import java.io.File
        |import org.apache.avro._
        |val parser = new Schema.Parser()
        |val schema = parser.parse(new File("schema/example.avsc"))
        |
      """.stripMargin,
    macroAnnotationSettings
  )