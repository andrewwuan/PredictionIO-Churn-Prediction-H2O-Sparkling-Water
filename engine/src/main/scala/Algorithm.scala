package org.template.vanilla

import io.prediction.controller.P2LAlgorithm
import io.prediction.controller.Params
import io.prediction.controller.IPersistentModel

import hex.deeplearning.DeepLearning
import hex.deeplearning.DeepLearningModel
import hex.deeplearning.DeepLearningModel.DeepLearningParameters
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.h2o._

import grizzled.slf4j.Logger

case class AlgorithmParams(mult: Int) extends Params

class Algorithm(val ap: AlgorithmParams)
  // extends PAlgorithm if Model contains RDD[]
  extends P2LAlgorithm[PreparedData, Model, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, data: PreparedData): Model = {

    val h2oContext = new H2OContext(sc).start()
    import h2oContext._

    //
    // -- TODO: more ways to filter & combine data
    //
    val customerTable: DataFrame = createDataFrame(data.customers);

    //
    // -- Run DeepLearning
    //
    val dlParams = new DeepLearningParameters()
    dlParams._train = customerTable
    dlParams._response_column = 'churn
    dlParams._epochs = 100

    val dl = new DeepLearning(dlParams)
    new Model(dlModel = dl.trainModel.get, table = customerTable, 
        h2oContext = h2oContext)

  }

  def predict(model: Model, query: Query): PredictedResult = {

    //
    // -- Make prediction
    //
    import model.h2oContext._
    val predictionH2OFrame = model.dlModel.score(model.table)('predict)
    val predictionFromModel = toRDD[DoubleHolder](predictionH2OFrame)
        .map( _.result.getOrElse(Double.NaN) ).collect

    //
    // -- Build response
    //
    val buf = new StringBuilder
    predictionFromModel.take(10).foreach(line => buf ++= line.toString)
    val prediction = buf.toString

    PredictedResult(p = prediction)
  }
}

class Model(val dlModel: DeepLearningModel, val table: DataFrame,
    val h2oContext: H2OContext)
    extends IPersistentModel[Params] {
    def save(id: String, params: Params, sc: SparkContext): Boolean = {
        false
    }
}
