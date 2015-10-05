import grizzled.slf4j.Logger
import io.prediction.controller.{EmptyActualResult, EmptyEvaluationInfo, PDataSource, Params}
import io.prediction.data.storage.Event
import io.prediction.data.store.PEventStore
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

case class DataSourceParams(appName: String) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {

    // read all events of EVENT involving ENTITY_TYPE and TARGET_ENTITY_TYPE
    val eventsRDD: RDD[Event] = PEventStore.find(
      appName = dsp.appName,
      entityType = Some("customer"),
      eventNames = Some(List("customer")))(sc)

    val customersRDD: RDD[Customer] = eventsRDD.map { event =>
      val customer = try {
        event.event match {
          case "customer" =>
            Customer(Some(event.entityId),
              Some(event.properties.get[Boolean]("intl_plan")),
              Some(event.properties.get[Boolean]("voice_mail_plan")),
              Some(event.properties.get[Long]("num_vmail_msg")),
              Some(event.properties.get[Double]("total_day_mins")),
              Some(event.properties.get[Long]("total_day_calls")),
              Some(event.properties.get[Double]("total_day_charge")),
              Some(event.properties.get[Double]("total_eve_mins")),
              Some(event.properties.get[Long]("total_eve_calls")),
              Some(event.properties.get[Double]("total_eve_charge")),
              Some(event.properties.get[Double]("total_night_mins")),
              Some(event.properties.get[Long]("total_night_calls")),
              Some(event.properties.get[Double]("total_night_charge")),
              Some(event.properties.get[Double]("total_intl_mins")),
              Some(event.properties.get[Long]("total_intl_calls")),
              Some(event.properties.get[Double]("total_intl_charge")),
              Some(event.properties.get[Long]("customer_service_calls")),
              Some(event.properties.get[Boolean]("churn")))
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

@SerialVersionUID(9129684718267757690L) case class Customer(
                                                             id: Option[String],
                                                             intlPlan: Option[Boolean],
                                                             voiceMailPlan: Option[Boolean],
                                                             numVmailMsg: Option[Long],
                                                             totalDayMins: Option[Double],
                                                             totalDayCalls: Option[Long],
                                                             totalDayCharge: Option[Double],
                                                             totalEveMins: Option[Double],
                                                             totalEveCalls: Option[Long],
                                                             totalEveCharge: Option[Double],
                                                             totalNightMins: Option[Double],
                                                             totalNightCalls: Option[Long],
                                                             totalNightCharge: Option[Double],
                                                             totalIntlMins: Option[Double],
                                                             totalIntlCalls: Option[Long],
                                                             totalIntlCharge: Option[Double],
                                                             customerServiceCalls: Option[Long],
                                                             churn: Option[Boolean]
                                                             ) extends Serializable

class TrainingData(
                    val customers: RDD[Customer]
                    ) extends Serializable {
  override def toString = {
    s"customers: [${customers.count()}] (${customers.take(2).toList}...)"
  }
}
