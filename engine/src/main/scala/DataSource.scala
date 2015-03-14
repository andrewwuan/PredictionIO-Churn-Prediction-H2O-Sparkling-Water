package org.template.vanilla

import io.prediction.controller.PDataSource
import io.prediction.controller.EmptyEvaluationInfo
import io.prediction.controller.EmptyActualResult
import io.prediction.controller.Params
import io.prediction.data.storage.Event
import io.prediction.data.storage.Storage

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import grizzled.slf4j.Logger

case class DataSourceParams(appId: Int) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {
    val eventsDb = Storage.getPEvents()
    // read all events of EVENT involving ENTITY_TYPE and TARGET_ENTITY_TYPE
    val eventsRDD: RDD[Event] = eventsDb.find(
      appId = dsp.appId,
      entityType = Some("ENTITY_TYPE"),
      eventNames = Some(List("EVENT")),
      targetEntityType = Some(Some("TARGET_ENTITY_TYPE")))(sc)

    val customersRDD: RDD[Customer] = eventsRDD.map { event =>
      val customer = try {
        event.event match {
          case "customer" =>
            Customer(event.entityId,
              event.intlPlan.get,
              event.voiceMailPlan.get,
              event.numVmailMsg.get,
              event.totalDayMins.get,
              event.totalDayCalls.get,
              event.totalDayCharge.get,
              event.totalEveMins.get,
              event.totalEveCalls.get,
              event.totalEveCharge.get,
              event.totalNightMins.get,
              event.totalNightCalls.get,
              event.totalNightCharge.get,
              event.totalIntlMins.get,
              event.totalIntlCalls.get,
              event.totalIntlCharge.get,
              event.customerServiceCalls.get,
              event.churn.get)
          case _ => throw new Exception(s"Unexpected event ${event} is read.")
        } catch {
          case e: Exception => {
            logger.error(s"Cannot convert ${event} to Rating. Exception: ${e}.")
            throw e
          }
        }
        customer
    }.cache()

    new TrainingData(customersRDD)
  }
}

case class Customer(
  id: Long,
  intlPlan: Boolean,
  voiceMailPlan: Boolean,
  numVmailMsg: Long,
  totalDayMins: Double,
  totalDayCalls: Long,
  totalDayCharge: Double,
  totalEveMins: Double,
  totalEveCalls: Long,
  totalEveCharge: Double,
  totalNightMins: Double,
  totalNightCalls: Long,
  totalNightCharge: Double,
  totalIntlMins: Double,
  totalIntlCalls: Long,
  totalIntlCharge: Double,
  customerServiceCalls: Long,
  churn: Boolean
)

class TrainingData(
  val customers: RDD[Event]
) extends Serializable {
  override def toString = {
    s"events: [${events.count()}] (${events.take(2).toList}...)"
  }
}
