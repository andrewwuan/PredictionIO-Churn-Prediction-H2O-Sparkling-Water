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
      entityType = Some("customer"),
      eventNames = Some(List("customer")))(sc)

    val customersRDD: RDD[Customer] = eventsRDD.map { event =>
      val customer = try {
        event.event match {
          case "customer" =>
            Customer(event.entityId,
              event.properties.get[Boolean]("intl_plan"),
              event.properties.get[Boolean]("voice_mail_plan"),
              event.properties.get[Long]("num_vmail_msg"),
              event.properties.get[Double]("total_day_mins"),
              event.properties.get[Long]("total_day_calls"),
              event.properties.get[Double]("total_day_charge"),
              event.properties.get[Double]("total_eve_mins"),
              event.properties.get[Long]("total_eve_calls"),
              event.properties.get[Double]("total_eve_charge"),
              event.properties.get[Double]("total_night_mins"),
              event.properties.get[Long]("total_night_calls"),
              event.properties.get[Double]("total_night_charge"),
              event.properties.get[Double]("total_intl_mins"),
              event.properties.get[Long]("total_intl_calls"),
              event.properties.get[Double]("total_intl_charge"),
              event.properties.get[Long]("customer_service_calls"),
              event.properties.get[Boolean]("churn"))
          case _ => throw new Exception(s"Unexpected event ${event} is read.")
        }
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
  id: String,
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
  val customers: RDD[Customer]
) extends Serializable {
  override def toString = {
    s"customers: [${customers.count()}] (${customers.take(2).toList}...)"
  }
}
