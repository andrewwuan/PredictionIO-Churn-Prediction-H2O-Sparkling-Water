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

case class AlgorithmParams(
    epochs: Int) extends Params

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
    dlParams._epochs = ap.epochs

    val dl = new DeepLearning(dlParams)
    new Model(dlModel = dl.trainModel.get, sc = sc, h2oContext = h2oContext)

  }

  def predict(model: Model, query: Query): PredictedResult = {

    //
    // -- Make prediction
    //
    import model.h2oContext._

    // Build customer from query and convert into data frame
    val customerFromQuery = Customer(
        None, Some(query.intlPlan), Some(query.voiceMailPlan),
        Some(query.numVmailMsg),
        Some(query.totalDayMins), Some(query.totalDayCalls),
        Some(query.totalDayCharge), Some(query.totalEveMins),
        Some(query.totalEveCalls), Some(query.totalEveCharge),
        Some(query.totalNightMins), Some(query.totalNightCalls),
        Some(query.totalNightCharge), Some(query.totalIntlMins),
        Some(query.totalIntlCalls), Some(query.totalIntlCharge),
        Some(query.customerServiceCalls), None)
    val customerQueryRDD = model.sc.parallelize(Array(customerFromQuery))
    val customerQueryDataFrame = createDataFrame(customerQueryRDD)

    // Predict using the data frame made
    val predictionH2OFrame = model.dlModel.score(customerQueryDataFrame)('predict)
    val predictionFromModel = toRDD[DoubleHolder](predictionH2OFrame)
        .map( _.result.getOrElse(Double.NaN) ).collect

    PredictedResult(p = predictionFromModel(0).toString)
  }
}

class Model(val dlModel: DeepLearningModel, val sc: SparkContext,
    val h2oContext: H2OContext)
    extends IPersistentModel[AlgorithmParams] {

    // Sparkling water models are not deserialization-friendly
    def save(id: String, params: AlgorithmParams, sc: SparkContext): Boolean = {
        false
    }
}
