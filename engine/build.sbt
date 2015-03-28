name := "PredictionIO-Churn-Prediction-H2O-Sparkling-Water"

organization := "io.prediction"


libraryDependencies ++= Seq(
  "io.prediction"    %% "core"          % pioVersion.value % "provided",
  "org.apache.spark" %% "spark-core"    % "1.2.0" % "provided",
  "org.apache.spark" %% "spark-mllib"   % "1.2.0" % "provided",
  "org.slf4j" % "slf4j-api" % "1.6.1",
  "org.apache.hadoop" % "hadoop-client" % "2.5.0",
  "org.apache.hadoop" % "hadoop-common" % "2.2.0",
  "ai.h2o" % "sparkling-water-core_2.10" % "0.2.9"
)

// META-INF discarding
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  }
}