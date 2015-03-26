package org.template.vanilla

import io.prediction.controller.P2LAlgorithm
import io.prediction.controller.Params

import hex.deeplearning.DeepLearning
import hex.deeplearning.DeepLearningModel
import hex.deeplearning.DeepLearningModel.DeepLearningParameters
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SchemaRDD}
import org.apache.spark.h2o._
import water.fvec.Frame

import grizzled.slf4j.Logger

case class AlgorithmParams(mult: Int) extends Params

class Algorithm(val ap: AlgorithmParams)
  // extends PAlgorithm if Model contains RDD[]
  extends P2LAlgorithm[PreparedData, Model, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, data: PreparedData): Model = {

    val h2oContext = new H2OContext(sc).start()
    import h2oContext._

    val sqlContext = new SQLContext(data.customers.context);
    import sqlContext._

    // -- TODO: Import other tables too
    data.customers.registerTempTable("Customers")

    //
    // -- TODO: Select useful data and join tables
    //
    val customers: RDD[Customer] = data.customers;
    val query = "SELECT * FROM customers"
    val table = sql(query)

    //
    // -- Run DeepLearning
    //
    val dlParams = new DeepLearningParameters()
    dlParams._train = table( 'intlPlan, 'voiceMailPlan, 'numVmailMsg,
        'totalDayMins, 'totalDayCalls, 'totalDayCharge,
        'totalEveMins, 'totalEveCalls, 'totalEveCharge,
        'totalNightMins, 'totalNightCalls, 'totalNightCharge,
        'totalIntlMins, 'totalIntlCalls, 'totalIntlCharge,
        'customerServiceCalls, 'churn)
    dlParams._response_column = 'churn
    dlParams._epochs = 100

    val dl = new DeepLearning(dlParams)
    new Model(dlModel = dl.trainModel.get, table = table, h2oContext = h2oContext)

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

class Model(val dlModel: DeepLearningModel, val table: SchemaRDD,
    val h2oContext: H2OContext)
    extends Serializable {
}
