name := "template-scala-parallel-vanilla"

organization := "io.prediction"


libraryDependencies ++= Seq(
  "io.prediction"    %% "core"          % "0.8.6" % "provided",
  "org.apache.spark" %% "spark-core"    % "1.2.0" % "provided",
  "org.apache.spark" %% "spark-mllib"   % "1.2.0" % "provided")

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1"
libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "2.5.0"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.2.0"
libraryDependencies += "ai.h2o" % "sparkling-water-core_2.10" % "0.2.9"

assemblyMergeStrategy in assembly := {
    case x => MergeStrategy.discard
}