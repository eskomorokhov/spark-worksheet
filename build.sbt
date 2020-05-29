import sbt.Keys.{resolvers, _}
import sbt._

val baseSettings = Seq(
  scalaVersion := "2.11.12",
  fork in Test := true,
  javaOptions in Test += "-Xmx4G",
  concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),
  // Because of a bug in sbt-assembly we can't exclude scala directly, so we do it in the next block
  // Excluding scala jars
)


val v = new {
  val scalatest     = "3.0.4"
  val scalamock     = "3.6.0"

  val hadoop = "3.1.1"
}

val baseDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2",

  "org.scalatest" %% "scalatest" % v.scalatest % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % v.scalamock % "test"
)

val helperDependencies = Seq(
  "org.scalaj" %% "scalaj-http" % "2.4.2" //excludeAll(ExclusionRule(organization = "com.fasterxml.jackson*"))
)

val hadoopDependencies = Seq(
  "org.apache.hadoop" % "hadoop-common"                 % v.hadoop,
  "org.apache.hadoop" % "hadoop-mapreduce-client-core"  % v.hadoop
).map(d => d % "provided" excludeAll(ExclusionRule(organization = "com.fasterxml.jackson*")))


val sparkDependencies = baseDependencies ++
  hadoopDependencies ++
  helperDependencies ++
  Seq(
    "com.fasterxml.jackson.core" % "jackson-annotations"                  % "2.6.7",
    "com.fasterxml.jackson.core" % "jackson-core"                         % "2.6.7",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7",
    "org.apache.spark" %% "spark-core" % "2.4.5",
    "org.apache.spark" %% "spark-sql" % "2.4.5",
    "org.apache.spark" %% "spark-hive" % "2.4.5"
)


lazy val worksheets = project
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies ++= sparkDependencies,
    name := "spark-ws"
  )
